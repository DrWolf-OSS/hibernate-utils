package it.drwolf.base.daos;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import it.drwolf.base.daos.common.PaginatedData;
import it.drwolf.base.interfaces.Loggable;
import it.drwolf.base.model.entities.BaseEntity;
import play.Logger.ALogger;
import play.db.jpa.JPAApi;

public class BaseEntityDAO<T extends BaseEntity> implements Loggable {

	public enum OrderType {
		ASC, DESC;
	}

	private static final String DEFAULT_PERSISTENCE_UNIT = "defaultPersistenceUnit";

	protected Class<T> resourceClass;

	protected JPAApi jpaApi;

	protected ALogger logger = this.getLogger();

	@Inject
	@SuppressWarnings("unchecked")
	public BaseEntityDAO(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
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

	public Long countAll() {
		CriteriaBuilder criteriaBuilder = this.jpaApi.em(DEFAULT_PERSISTENCE_UNIT).getCriteriaBuilder();
		CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
		countQuery.select(criteriaBuilder.count(countQuery.from(this.resourceClass)));
		return this.jpaApi.em("Default").createQuery(countQuery).getSingleResult();
	}

	public void create(T entity) {
		this.jpaApi.em(DEFAULT_PERSISTENCE_UNIT).persist(entity);
	}

	public void delete(T entity) {
		this.jpaApi.em(DEFAULT_PERSISTENCE_UNIT).remove(entity);
	}

	public T find(Object id) {
		return this.jpaApi.em(DEFAULT_PERSISTENCE_UNIT).find(this.resourceClass, id);
	}

	public List<T> getAll() {
		return this.jpaApi.em(DEFAULT_PERSISTENCE_UNIT)
				.createQuery("from " + this.resourceClass.getName(), this.resourceClass).getResultList();
	}

	public PaginatedData<T> getAllPaginated(String orderCol, OrderType orderType, int page, int size) {
		final Long total = this.countAll();
		final List<Predicate> predicates = new ArrayList<>();
		CriteriaBuilder criteriaBuilder = this.jpaApi.em(DEFAULT_PERSISTENCE_UNIT).getCriteriaBuilder();

		CriteriaQuery<T> query = criteriaBuilder.createQuery(this.resourceClass);
		Root<T> rootItemDefinition = query.from(this.resourceClass);
		query.select(rootItemDefinition).where(predicates.toArray(new Predicate[predicates.size()]));

		if (orderType != null && orderType.equals(OrderType.ASC)) {
			query.orderBy(criteriaBuilder.asc(rootItemDefinition.get(orderCol)));
		} else if (orderType != null && orderType.equals(OrderType.DESC)) {
			query.orderBy(criteriaBuilder.desc(rootItemDefinition.get(orderCol)));
		}

		final int first = this.calculateFirstResult(page, size);
		List<T> elements = this.jpaApi.em(DEFAULT_PERSISTENCE_UNIT).createQuery(query).setFirstResult(first)
				.setMaxResults(size).getResultList();

		return new PaginatedData<>(elements, page, size, total.intValue());
	}

	public List<T> getByIdSet(Set<? extends Object> ids) {
		if (!ids.isEmpty()) {
			return this.jpaApi.em(DEFAULT_PERSISTENCE_UNIT)
					.createQuery("FROM " + this.resourceClass.getName() + " en WHERE en.id IN(:ids)",
							this.resourceClass)
					.setParameter("ids", ids).getResultList();
		} else {
			return new ArrayList<T>();
		}
	}

	public List<T> getByIdSetNegate(Set<? extends Object> ids) {
		if (!ids.isEmpty()) {
			return this.jpaApi.em(DEFAULT_PERSISTENCE_UNIT)
					.createQuery("FROM " + this.resourceClass.getName() + " en WHERE en.id NOT IN(:ids)",
							this.resourceClass)
					.setParameter("ids", ids).getResultList();
		} else {
			return this.getAll();
		}
	}

	public EntityManager getDefaultEntityManager() {
		return this.jpaApi.em(DEFAULT_PERSISTENCE_UNIT);
	}

	public T update(T entity) {
		return this.jpaApi.em(DEFAULT_PERSISTENCE_UNIT).merge(entity);
	}

}
