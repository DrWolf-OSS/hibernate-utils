package it.drwolf.jpa;

import java.lang.reflect.ParameterizedType;
import java.util.function.Function;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.drwolf.jwt.JWTUtils;
import play.db.jpa.JPAApi;
import play.mvc.Http.Request;

public class RevisionJPAApi {


	public static final ThreadLocal<ObjectNode> currentUser = new ThreadLocal<>();


	public RevisionJPAApi() {

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
					RevisionJPAApi.currentUser.set(this.jwtUtils.getUser(request));
				} catch (Exception e) {
					RevisionJPAApi.currentUser.remove();
				}
			}
			return func.apply(em);
		});
	}

}