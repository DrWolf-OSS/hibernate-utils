package it.drwolf.jpa;

import java.lang.reflect.ParameterizedType;
import java.util.function.Function;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import it.drwolf.jwt.JWTUtils;
import play.db.jpa.JPAApi;
import play.mvc.Http.Request;

public class RevisionJPAApi<U> {

	private final Class<U> resourceClass;

	public static final ThreadLocal<Object> currentUser = new ThreadLocal<>();


	public RevisionJPAApi() {
		this.resourceClass = (Class<U>) ((ParameterizedType) this.getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	@Inject
	private JPAApi jpaApi;

	@Inject
	private JWTUtils jwtUtils;


	public <T> T withReadOnlyTransaction(Function<EntityManager, T> func) {
		return this.jpaApi.withTransaction("default", true, func::apply);
	}

	public <T> T withTransaction(Request request, Function<EntityManager, T> func) {
		return this.jpaApi.withTransaction(em -> {
			RevisionJPAApi.currentUser.remove();
			if (request != null) {
				try {
					RevisionJPAApi.currentUser.set(this.jwtUtils.getUser(request,resourceClass));
				} catch (Exception e) {
					RevisionJPAApi.currentUser.remove();
				}
			}
			return func.apply(em);
		});
	}

}