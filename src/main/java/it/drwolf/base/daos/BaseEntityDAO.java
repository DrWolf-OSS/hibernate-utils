package it.drwolf.base.daos;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import it.drwolf.base.daos.common.OrderParameter;
import it.drwolf.base.daos.common.PageParameter;
import it.drwolf.base.daos.common.PaginatedData;
import it.drwolf.base.daos.common.QueryManager;
import it.drwolf.base.daos.common.filter.FilterParameter;
import it.drwolf.base.interfaces.Loggable;
import it.drwolf.base.model.entities.BaseEntity;
import it.drwolf.exceptions.HttpException;

/**
 * BaseEntityDAO is an abstract DAO that provides basic functionalities to manage
 * entities, in order to use it your entities must extend BaseEntity and your
 * DAOs must extend BaseEntityDAO.
 *
 * @param <T>
 * @author spaladini
 */
public abstract class BaseEntityDAO<T extends BaseEntity> implements Loggable {

	protected final Class<T> resourceClass;

	public enum QueryType {
		ENTITIES, IDS, COUNT
	}

	@SuppressWarnings("unchecked")
	public BaseEntityDAO() {
		this.resourceClass = (Class<T>) ((ParameterizedType) this.getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	protected <V> CriteriaQuery<V> buildCriteriaQuery(EntityManager em, QueryType queryType, Class<V> clazz,
			Set<FilterParameter> filters, OrderParameter order) {

		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

		CriteriaQuery<V> query = criteriaBuilder.createQuery(clazz);
		Root<T> root = query.from(this.resourceClass);

		final QueryManager<T> queryManager = new QueryManager<>(root);
		final List<Predicate> predicates = queryManager.buildPredicatesList(criteriaBuilder, filters);

		if (queryType.equals(QueryType.COUNT)) {
			query.select((Selection<? extends V>) criteriaBuilder.countDistinct(root.get("id")));
		} else if (queryType.equals(QueryType.IDS)) {
			query.select(root.get("id")).groupBy(root.get("id"));
		} else if (queryType.equals(QueryType.ENTITIES)) {
			query.select((Selection<? extends V>) root).groupBy(root.get("id"));
		}

		if (order != null && !queryType.equals(QueryType.COUNT)) {
			Order orderBy = queryManager.buildCriteriaOrder(criteriaBuilder, order, true);
			query.orderBy(orderBy);
		}

		return query.where(predicates.toArray(new Predicate[predicates.size()]));
	}

	protected int calculateFirstResult(int page, int size) {
		int first = 0;
		if (page * size > size) {
			first = (page * size) - size;
		}
		return first;
	}

	/**
	 * Return a count of all entities of specified type
	 *
	 * @param em
	 * @return total count
	 */
	public Long countAll(EntityManager em) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
		countQuery.select(criteriaBuilder.count(countQuery.from(this.resourceClass)));
		return em.createQuery(countQuery).getSingleResult();
	}

	/**
	 * Create an entity instance of specified type
	 *
	 * @param em
	 * @return
	 */
	protected void create(EntityManager em, T entity) {
		em.persist(entity);
	}

	/**
	 * Delete the entity instance
	 *
	 * @param em
	 * @param entity
	 */
	public void delete(EntityManager em, T entity) {
		em.remove(entity);
	}

	/**
	 * Find by primary key.<br>
	 * Return an Optional containing (or not) an entity of the specified type
	 *
	 * @param em
	 * @param id: primary key (@Id) of the entity instance
	 * @return an Optional of nullable
	 */
	public Optional<T> find(EntityManager em, Object id) {
		return Optional.ofNullable(em.find(this.resourceClass, id));
	}

	/**
	 * Find by primary key.<br>
	 * Return an entity of the specified type
	 *
	 * @param em
	 * @param id: primary key (@Id) of the entity instance
	 * @return an Optional of nullable
	 * @throws HttpException (NOT_FOUND) if not present
	 */
	public T get(EntityManager em, Object id) {
		return this.find(em, id)
				.orElseThrow(() -> new HttpException(String.format("%s #%s not found", this.resourceClass, id),
						HttpException.Status.NOT_FOUND));
	}

	/**
	 * Return all entities of specified type
	 *
	 * @param em
	 * @return
	 */
	public List<T> getAll(EntityManager em) {
		return em.createQuery("from " + this.resourceClass.getName(), this.resourceClass).getResultList();
	}

	/**
	 * Return a single page of all entities of specified type
	 *
	 * @param em
	 * @param page: PageParameter instance with info about page number, page size
	 *              and sorting
	 * @return an instance of PaginatedData
	 */
	public PaginatedData<T> getAll(EntityManager em, PageParameter page) {
		return this.getAll(em, null, page);
	}

	/**
	 * Return a single page of entities of specified type sorted by info contained
	 * in OrderParameter
	 *
	 * @param em
	 * @param page:  contain pagination info
	 * @param order: contains sorting info
	 * @return an instance of PaginatedData
	 */
	public PaginatedData<T> getAll(EntityManager em, OrderParameter order, PageParameter page) {
		final Long total = this.countAll(em);
		final List<Predicate> predicates = new ArrayList<>();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

		CriteriaQuery<T> query = criteriaBuilder.createQuery(this.resourceClass);
		Root<T> rootItemDefinition = query.from(this.resourceClass);
		query.select(rootItemDefinition).where(predicates.toArray(new Predicate[predicates.size()]));

		this.handleOrderParameter(criteriaBuilder, query, rootItemDefinition, order);

		final int first = this.calculateFirstResult(page.getPageNumber(), page.getSize());
		List<T> elements = em.createQuery(query).setFirstResult(first).setMaxResults(page.getSize()).getResultList();

		return new PaginatedData<>(elements, page.getPageNumber(), page.getSize(), total.intValue());
	}

	/**
	 * Return all entities of specified type that have their id in the ids set
	 *
	 * @param em
	 * @param ids
	 * @return
	 */
	public List<T> getIfInIdSet(EntityManager em, Set<? extends Object> ids) {
		if (!ids.isEmpty()) {
			return em.createQuery("FROM " + this.resourceClass.getName() + " en WHERE en.id IN(:ids)",
					this.resourceClass).setParameter("ids", ids).getResultList();
		} else {
			return new ArrayList<>();
		}
	}

	/**
	 * Return all entities of specified type that don't have their id in the ids set
	 *
	 * @param em
	 * @param ids
	 * @return
	 */
	public List<T> getIfNotInIdSet(EntityManager em, Set<? extends Object> ids) {
		if (!ids.isEmpty()) {
			return em.createQuery("FROM " + this.resourceClass.getName() + " en WHERE en.id NOT IN(:ids)",
					this.resourceClass).setParameter("ids", ids).getResultList();
		} else {
			return this.getAll(em);
		}
	}

	@Deprecated
	protected void handleOrderParameter(CriteriaBuilder criteriaBuilder, CriteriaQuery<T> query, Root<T> rootElement,
			OrderParameter order) {
		if (order != null) {
			if (order.getOrderType().equals(OrderParameter.OrderType.ASC)) {
				query.orderBy(criteriaBuilder.asc(rootElement.get(order.getOrderField())));
			} else if (order.getOrderType().equals(OrderParameter.OrderType.DESC)) {
				query.orderBy(criteriaBuilder.desc(rootElement.get(order.getOrderField())));
			}
		}
	}

	/**
	 * Persiste or update an entity of specified type and return it
	 *
	 * @param em
	 * @param entity
	 * @return
	 */
	public T save(EntityManager em, T entity) {
		if (entity.getId() == null) {
			this.create(em, entity);
			return entity;
		} else {
			return this.update(em, entity);
		}
	}

	/**
	 * Return a single page of entities of specified type sorted by info contained in OrderParameter
	 * and filtered by a list of FilterParameter. All filters are combined in AND.
	 *
	 * @param em
	 * @param filters
	 * @param order
	 * @param page
	 * @return
	 */
	public PaginatedData<T> search(EntityManager em, Set<FilterParameter> filters, OrderParameter order,
			PageParameter page) {

		CriteriaQuery<Long> createCount = this.buildCriteriaQuery(em, QueryType.COUNT, Long.class, filters, order);
		Long count = em.createQuery(createCount).getSingleResult();

		CriteriaQuery<T> query = this.buildCriteriaQuery(em, QueryType.ENTITIES, this.resourceClass, filters, order);
		final TypedQuery<T> typedQuery = em.createQuery(query);
		final int first = this.calculateFirstResult(page.getPageNumber(), page.getSize());
		List<T> results = typedQuery.setFirstResult(first).setMaxResults(page.getSize()).getResultList();

		return new PaginatedData<>(results, page.getPageNumber(), page.getSize(), count.intValue());
	}

	/**
	 * Return a list of entities of specified type sorted by info contained in OrderParameter and filtered by
	 * a list of FilterParameter.  All filters are combined in AND.
	 *
	 * @param em
	 * @param filters
	 * @param order
	 * @return
	 */
	public List<T> search(EntityManager em, Set<FilterParameter> filters, OrderParameter order) {
		CriteriaQuery<T> query = this.buildCriteriaQuery(em, QueryType.ENTITIES, this.resourceClass, filters, order);
		final TypedQuery<T> typedQuery = em.createQuery(query);
		return typedQuery.getResultList();
	}

	protected T update(EntityManager em, T entity) {
		T updated = em.merge(entity);
		em.flush();
		return updated;
	}

}
