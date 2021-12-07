package it.drwolf.base.daos.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.hibernate.query.criteria.internal.JoinImplementor;
import org.slf4j.Logger;

import it.drwolf.base.daos.common.exceptions.FilterParameterException;
import it.drwolf.base.daos.common.exceptions.JoinMappingException;
import it.drwolf.base.interfaces.Loggable;
import it.drwolf.base.model.entities.BaseEntity;

/**
 * Utility class that provides functionalities to create and retrieve joins and manage CriteriaQuery
 *
 * @param <T>
 * @author spaladini
 */
public class JoinManager<T> implements Loggable {

	private Logger logger = this.logger();

	private Root<T> root;

	private Map<String, Join> joinMap = new HashMap<>();

	public JoinManager(Root<T> root) {
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

	public Order buildCriteriaOrder(CriteriaBuilder criteriaBuilder, OrderParameter order) {
		return this.buildCriteriaOrder(criteriaBuilder, order, null);
	}

	public Order buildCriteriaOrder(CriteriaBuilder criteriaBuilder, OrderParameter order, Boolean makeJoin) {
		Order orderBy;
		try {
			String columnName;
			if (order == null) {
				order = new OrderParameter("id", OrderParameter.OrderType.ASC);
			}

			Method orderMethod = criteriaBuilder.getClass()
					.getMethod(order.getOrderType().name().toLowerCase(Locale.ROOT), Expression.class);
			if (order.getOrderField().indexOf(".") < 0) {
				columnName = order.getOrderField();
				orderBy = (Order) orderMethod.invoke(criteriaBuilder, this.getRoot().get(columnName));
			} else {
				int lastIndexOfDot = order.getOrderField().lastIndexOf(".");
				columnName = order.getOrderField().substring(lastIndexOfDot + 1);
				String joinPath = order.getOrderField().substring(0, lastIndexOfDot);

				Join join;
				if (makeJoin != null && makeJoin) {
					// Se la join che serve per l'ordinamento non è presente viene creata
					join = this.findOrMakeJoin(joinPath);
				} else {
					// La join che serve per l'ordinamento viene cercata tra quelle usate nei filtri
					join = this.getJoin(joinPath);
				}

				if (join == null) {
					throw new FilterParameterException(
							String.format("Field '%s.%s' not reachable", joinPath, columnName));
				}
				orderBy = (Order) orderMethod.invoke(criteriaBuilder, join.get(columnName));
			}
		} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
			this.logger.error("Error managing ordering {}. Fall back on orderBy \"id\" ASC", order, e);
			orderBy = criteriaBuilder.asc(this.getRoot().get("id"));
		}
		return orderBy;
	}

	private void checkPreviousPath(String path) {
		String previousPath = this.getPreviousPath(path);
		if (previousPath != null && this.joinMap.get(previousPath) == null) {
			throw new JoinMappingException(String.format("Previous join '%s' not found!", previousPath));
		}
	}

	public Join<BaseEntity, BaseEntity> findOrMakeJoin(String path) {
		Join<BaseEntity, BaseEntity> join = this.joinMap.get(path);
		if (join != null) {
			return join;
		}

		String joinAttributeName = this.getJoinAttributeName(path);
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

	public Join getJoin(String path) {
		return this.joinMap.get(path);
	}

	private String getJoinAttributeName(String path) {
		if (path.indexOf(".") < 0) {
			return path;
		} else {
			int lastIndexOfDot = path.lastIndexOf(".");
			return path.substring(lastIndexOfDot + 1);
		}
	}

	private String getPreviousPath(String path) {
		int lastIndexOfDot = path.lastIndexOf(".");
		if (lastIndexOfDot > -1) {
			return path.substring(0, lastIndexOfDot);
		}
		return null;
	}

	public Root<T> getRoot() {
		return this.root;
	}
}
