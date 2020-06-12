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

public class BaseEntityDAO<T extends BaseEntity> implements Loggable {

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

	public Long countAll(EntityManager em) {
		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
		countQuery.select(criteriaBuilder.count(countQuery.from(this.resourceClass)));
		return em.createQuery(countQuery).getSingleResult();
	}

	protected void create(EntityManager em, T entity) {
		em.persist(entity);
	}

	public void delete(EntityManager em, T entity) {
		em.remove(entity);
	}

	public Optional<T> find(EntityManager em, Object id) {
		return Optional.ofNullable(em.find(this.resourceClass, id));
	}

	public List<T> getAll(EntityManager em) {
		return em.createQuery("from " + this.resourceClass.getName(), this.resourceClass).getResultList();
	}

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

	public List<T> getIfInIdSet(EntityManager em, Set<? extends Object> ids) {
		if (!ids.isEmpty()) {
			return em.createQuery("FROM " + this.resourceClass.getName() + " en WHERE en.id IN(:ids)",
					this.resourceClass).setParameter("ids", ids).getResultList();
		} else {
			return new ArrayList<T>();
		}
	}

	public List<T> getIfNotInIdSet(EntityManager em, Set<? extends Object> ids) {
		if (!ids.isEmpty()) {
			return em.createQuery("FROM " + this.resourceClass.getName() + " en WHERE en.id NOT IN(:ids)",
					this.resourceClass).setParameter("ids", ids).getResultList();
		} else {
			return this.getAll(em);
		}
	}

	public T save(EntityManager em, T entity) {
		if (entity.getId() == null) {
			this.create(em, entity);
			return entity;
		} else {
			T res = this.update(em, entity);
			em.flush();
			return res;
		}
	}

	protected T update(EntityManager em, T entity) {
		return em.merge(entity);
	}

}
