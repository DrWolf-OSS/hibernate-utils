package it.drwolf.base.daos.common.filter;

import java.util.Collection;
import java.util.stream.Stream;

import it.drwolf.base.daos.common.exceptions.FilterParameterException;

public class CollectionAttributeFilter<T extends Collection> extends FilterParameter<T> {

	public CollectionAttributeFilter(String path, FilterOperator operator, T value) {
		super(path, operator, value);
		if (value != null) {
			this.checkOperatorWithValue(operator);
		} else {
			this.checkOperatorsWithoutValue(operator);
		}
	}

	public CollectionAttributeFilter(Stream<String> path, FilterOperator operator, T value) {
		super(path, operator, value);
		if (value != null) {
			this.checkOperatorWithValue(operator);
		} else {
			this.checkOperatorsWithoutValue(operator);
		}
	}

	public CollectionAttributeFilter(String path, FilterOperator operator) {
		super(path, operator, null);
		this.checkOperatorsWithoutValue(operator);
	}

	public CollectionAttributeFilter(Stream<String> path, FilterOperator operator) {
		super(path, operator, null);
		this.checkOperatorsWithoutValue(operator);
	}

	private void checkOperatorWithValue(FilterOperator operator) {
		if (!FilterOperator.collectionAttributeFiltersWithValue.contains(operator)) {
			throw new FilterParameterException(String.format("Operator %s not allowed!", operator));
		}
	}

	private void checkOperatorsWithoutValue(FilterOperator operator) {
		if (!FilterOperator.collectionAttributeFiltersWithNoValue.contains(operator)) {
			throw new FilterParameterException(String.format("Operator %s not allowed!", operator));
		}
	}

}
