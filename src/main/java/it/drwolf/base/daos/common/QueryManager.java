package it.drwolf.base.daos.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.query.criteria.internal.JoinImplementor;
import org.slf4j.Logger;

import it.drwolf.base.daos.common.exceptions.FilterParameterException;
import it.drwolf.base.daos.common.exceptions.JoinMappingException;
import it.drwolf.base.daos.common.filter.FilterParameter;
import it.drwolf.base.interfaces.Loggable;
import it.drwolf.base.model.entities.BaseEntity;

/**
 * Utility class that provides functionalities to build a javax.persistence.criteria.CriteriaQuery
 *
 * @param <T>
 * @author spaladini
 */
public class QueryManager<T> implements Loggable {

	private Logger logger = this.logger();

	private Root<T> root;

	private Map<String, Join> joinMap = new HashMap<>();

	public QueryManager(Root<T> root) {
		this.root = root;
	}

	public void addJoin(Join join) {
		String attrName = join.getAttribute().getName();
		String path = "";

		Path parentPath = join.getParentPath();
		while (parentPath instanceof JoinImplementor) {
			final JoinImplementor joinImplementor = (JoinImplementor) parentPath;
			path = joinImplementor.getAttribute().getName() + "." + path;
			parentPath = parentPath.getParentPath();
		}

		if (!(parentPath instanceof Root)) {
			throw new JoinMappingException("Root element not reachable");
		}

		String finalPath = path + attrName;
		this.checkPreviousPath(finalPath);

		this.joinMap.put(finalPath, join);
	}

	/**
	 * Build a javax.persistence.criteria.Order object from an OrderParameter.
	 *
	 * @param criteriaBuilder
	 * @param order
	 * @return
	 */
	public Order buildCriteriaOrder(CriteriaBuilder criteriaBuilder, OrderParameter order) {
		return this.buildCriteriaOrder(criteriaBuilder, order, null);
	}

	/**
	 * Build a javax.persistence.criteria.Order object from an OrderParameter.
	 *
	 * @param criteriaBuilder
	 * @param order
	 * @param makeJoin:       if the join involved in the sorting is not already present in the map: FALSE raise an Exception, TRUE make the join
	 * @return
	 */
	public Order buildCriteriaOrder(CriteriaBuilder criteriaBuilder, OrderParameter order, Boolean makeJoin) {
		if (order == null) {
			throw new IllegalArgumentException("OrderParameter can't be null");
		}

		try {
			Method orderMethod = criteriaBuilder.getClass()
					.getMethod(order.getOrderType().name().toLowerCase(Locale.ROOT), Expression.class);
			String attributeName = this.getAttributeName(order.getOrderField());

			if (this.isRootAttribute(order.getOrderField())) {
				return (Order) orderMethod.invoke(criteriaBuilder, this.getRoot().get(attributeName));
			} else {
				String joinPath = this.getPreviousPath(order.getOrderField());

				Join join;
				if (makeJoin != null && makeJoin) {
					// Se la join che serve per l'orientamento non è presente viene creata
					join = this.findOrMakeJoin(joinPath);
				} else {
					// La join che serve per l'orientamento viene cercata tra quelle usate nei filtri
					join = this.getJoin(joinPath);
				}

				if (join == null) {
					throw new FilterParameterException(
							String.format("Field '%s.%s' not reachable", joinPath, attributeName));
				}
				return (Order) orderMethod.invoke(criteriaBuilder, join.get(attributeName));
			}
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
			this.logger.error("Error managing ordering {}. Fall back on orderBy \"id\" ASC", order, e);
			return criteriaBuilder.asc(this.getRoot().get("id"));
		}
	}

	/**
	 * Build a javax.persistence.criteria.Predicate from a FilterParameter
	 *
	 * @param criteriaBuilder
	 * @param from
	 * @param filter
	 * @return
	 */
	public Predicate buildPredicate(CriteriaBuilder criteriaBuilder, From from, FilterParameter filter) {

		switch (filter.getOperator()) {

		case EQ:
			return criteriaBuilder.equal(from.get(filter.getFieldName()), filter.getValue());

		case GT:
			return criteriaBuilder.greaterThan(from.get(filter.getFieldName()), (Comparable) filter.getValue());

		case GE:
			return criteriaBuilder.greaterThanOrEqualTo(from.get(filter.getFieldName()),
					(Comparable) filter.getValue());

		case LT:
			return criteriaBuilder.lessThan(from.get(filter.getFieldName()), (Comparable) filter.getValue());

		case LE:
			return criteriaBuilder.lessThanOrEqualTo(from.get(filter.getFieldName()), (Comparable) filter.getValue());

		case LIKE:
			return criteriaBuilder.like(from.get(filter.getFieldName()), "%" + filter.getValue() + "%");

		case NOT_LIKE:
			return criteriaBuilder.notLike(from.get(filter.getFieldName()), "%" + filter.getValue() + "%");

		case IN:
			return criteriaBuilder.in(from.get(filter.getFieldName())).value(filter.getValue());

		case NOT_IN:
			return criteriaBuilder.not(criteriaBuilder.in(from.get(filter.getFieldName())).value(filter.getValue()));

		case IS_EMPTY:
			return criteriaBuilder.isEmpty(from.get(filter.getFieldName()));

		case IS_NULL:
			return criteriaBuilder.isNull(from.get(filter.getFieldName()));

		case IS_NOT_NULL:
			return criteriaBuilder.isNotNull(from.get(filter.getFieldName()));

		case IS_TRUE:
			return criteriaBuilder.isTrue(from.get(filter.getFieldName()));

		case IS_FALSE:
			return criteriaBuilder.isFalse(from.get(filter.getFieldName()));

		default:
			throw new FilterParameterException(String.format("Operator %s not implemented!", filter.getOperator()));
		}

	}

	/**
	 * Build a list of javax.persistence.criteria.Predicate from a set of FilterParameter
	 *
	 * @param criteriaBuilder
	 * @param filters
	 * @return
	 */
	public List<Predicate> buildPredicatesList(CriteriaBuilder criteriaBuilder, Set<FilterParameter> filters) {
		final List<Predicate> predicates = new ArrayList<>();

		for (FilterParameter filter : filters) {
			Predicate p;
			if (!filter.getJoinName().equals(FilterParameter.ROOT)) {
				Join<BaseEntity, BaseEntity> join = this.findOrMakeJoin(filter.getJoinName());
				p = this.buildPredicate(criteriaBuilder, join, filter);
			} else {
				p = this.buildPredicate(criteriaBuilder, this.root, filter);
			}
			predicates.add(p);
		}

		return predicates;
	}

	private void checkPreviousPath(String path) {
		String previousPath = this.getPreviousPath(path);
		if (previousPath != null && this.joinMap.get(previousPath) == null) {
			throw new JoinMappingException(String.format("Previous join '%s' not found!", previousPath));
		}
	}

	/**
	 * Find or make a javax.persistence.criteria.Join based on the "path"
	 *
	 * @param path
	 * @return
	 */
	public Join<BaseEntity, BaseEntity> findOrMakeJoin(String path) {
		Join<BaseEntity, BaseEntity> join = this.joinMap.get(path);
		if (join != null) {
			return join;
		}

		String joinAttributeName = this.getAttributeName(path);
		String previousPath = this.getPreviousPath(path);

		if (previousPath == null) {
			join = this.root.join(joinAttributeName, JoinType.LEFT);
		} else {
			Join previousJoin = this.joinMap.get(previousPath);
			if (previousJoin == null) {
				previousJoin = this.findOrMakeJoin(previousPath);
			}
			join = previousJoin.join(joinAttributeName);
		}
		this.addJoin(join);
		return join;
	}

	private String getAttributeName(String path) {
		if (this.isRootAttribute(path)) {
			return path;
		} else {
			int lastIndexOfDot = path.lastIndexOf(".");
			return path.substring(lastIndexOfDot + 1);
		}
	}

	/**
	 * Get a javax.persistence.criteria.Join based on the "path"
	 *
	 * @param path
	 * @return
	 */
	public Join getJoin(String path) {
		return this.joinMap.get(path);
	}

	private String getPreviousPath(String path) {
		if (!this.isRootAttribute(path)) {
			return path.substring(0, path.lastIndexOf("."));
		}
		return null;
	}

	/**
	 * Get the javax.persistence.criteria.Root of the query
	 *
	 * @return
	 */
	public Root<T> getRoot() {
		return this.root;
	}

	private boolean isRootAttribute(String path) {
		int lastIndexOfDot = path.lastIndexOf(".");
		if (lastIndexOfDot > -1) {
			return false;
		}
		return true;
	}

}
