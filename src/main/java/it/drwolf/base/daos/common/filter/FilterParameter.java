package it.drwolf.base.daos.common.filter;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.drwolf.base.daos.common.exceptions.FilterParameterException;

public abstract class FilterParameter<T> {

	public static final String ROOT = "ROOT";

	private String path;

	private T value;

	private FilterOperator operator;

	private String fieldName;

	private String joinName;

	public FilterParameter(String path, FilterOperator operator, T value) {

		this.path = path;
		this.operator = operator;
		this.value = value;

		this.readPath();
	}

	public FilterParameter(Stream<String> path, FilterOperator operator, T value) {

		this.path = path.collect(Collectors.joining("."));
		this.operator = operator;
		this.value = value;

		this.readPath();
	}

	public String getFieldName() {
		return this.fieldName;
	}

	public String getJoinName() {
		return this.joinName;
	}

	public FilterOperator getOperator() {
		return this.operator;
	}

	public String getPath() {
		return this.path;
	}

	Class<T> getTypeOfT() {
		if (this.value != null) {
			return (Class<T>) this.value.getClass();
		} else {
			throw new FilterParameterException("Cannot determine type of T, value is NULL");
		}
	}

	public T getValue() {
		return this.value;
	}

	private void readPath() {
		if (this.path.indexOf(".") < 0) {
			this.fieldName = this.path;
			this.joinName = FilterParameter.ROOT;
		} else {
			int lastIndexOfDot = this.path.lastIndexOf(".");
			this.fieldName = this.path.substring(lastIndexOfDot + 1);
			this.joinName = this.path.substring(0, lastIndexOfDot);
		}
	}

}
