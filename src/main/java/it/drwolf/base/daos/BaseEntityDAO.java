package it.drwolf.base.daos;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;

import it.drwolf.base.daos.common.PaginatedData;
import it.drwolf.base.interfaces.Loggable;
import it.drwolf.base.model.entities.BaseEntity;

/**
 * BasicEntityDAO is an abstract DAO that provides basic funcionalities to
 * manage entities, in order to use it your entities must extend BaseEntity and
 * your DAOs must extend BasicEntityDAO.
 *
 * @author spaladini
 *
 * @param <T>
 */
public abstract class BaseEntityDAO<T extends BaseEntity> implements Loggable {

	public enum OrderType {
		ASC, DESC;
	}

	protected final Logger logger = this.getLogger();

	protected final Class<T> resourceClass;

	@SuppressWarnings("unchecked")
	public BaseEntityDAO() {
		this.resourceClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	protected int calculateFirstResult(int page, int size) {
		int first = 0;
		if (page * size > size) {
			first = (page * size) - size;
		}
		return first;
	}

	/**
	 *
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
	 *
	 * Create an entity instance of specified type
	 *
	 * @param em
	 * @return
	 */
	protected void create(EntityManager em, T entity) {
		em.persist(entity);
	}

	/**
	 *
	 * Delete the entity instance
	 *
	 * @param em
	 * @param entity
	 */
	public void delete(EntityManager em, T entity) {
		em.remove(entity);
	}

	/**
	 *
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
	 *
	 * Return all entities of specified type
	 *
	 * @param em
	 * @return
	 */
	public List<T> getAll(EntityManager em) {
		return em.createQuery("from " + this.resourceClass.getName(), this.resourceClass).getResultList();
	}

	/**
	 *
	 * Return a single page of all entities of specified type order by "orderColumn"
	 *
	 * @param em
	 * @param orderCol:  ordered field
	 * @param orderType: ASC or DESC
	 * @param page:      number of the curret page
	 * @param size:      size of the current page
	 * @return PaginatedData: a single page of entities
	 */
	public PaginatedData<T> getAllPaginated(EntityManager em, String orderCol, OrderType orderType, int page,
			int size) {
		final Long total = this.countAll(em);
		final List<Predicate> predicates = new ArrayList<>();
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

		CriteriaQuery<T> query = criteriaBuilder.createQuery(this.resourceClass);
		Root<T> rootItemDefinition = query.from(this.resourceClass);
		query.select(rootItemDefinition).where(predicates.toArray(new Predicate[predicates.size()]));

		if (orderCol != null && orderType != null && orderType.equals(OrderType.ASC)) {
			query.orderBy(criteriaBuilder.asc(rootItemDefinition.get(orderCol)));
		} else if (orderCol != null && orderType != null && orderType.equals(OrderType.DESC)) {
			query.orderBy(criteriaBuilder.desc(rootItemDefinition.get(orderCol)));
		}

		final int first = this.calculateFirstResult(page, size);
		List<T> elements = em.createQuery(query).setFirstResult(first).setMaxResults(size).getResultList();

		return new PaginatedData<>(elements, page, size, total.intValue());
	}

	/**
	 *
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
			return new ArrayList<T>();
		}
	}

	/**
	 *
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

	/**
	 *
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

	protected T update(EntityManager em, T entity) {
		T updated = em.merge(entity);
		em.flush();
		return updated;
	}

}
