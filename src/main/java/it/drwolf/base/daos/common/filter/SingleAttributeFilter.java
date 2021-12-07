package it.drwolf.base.daos.common.filter;

import java.util.stream.Stream;

import it.drwolf.base.daos.common.exceptions.FilterParameterException;

public class SingleAttributeFilter<T> extends FilterParameter<T> {

	public SingleAttributeFilter(String path, FilterOperator operator, T value) {
		super(path, operator, value);
		if (value != null) {
			this.checkOperatorsWithValue(operator, value);
		} else {
			this.checkOperatorsWithoutValue(operator);
		}
	}

	public SingleAttributeFilter(Stream<String> path, FilterOperator operator, T value) {
		super(path, operator, value);
		if (value != null) {
			this.checkOperatorsWithValue(operator, value);
		} else {
			this.checkOperatorsWithoutValue(operator);
		}
	}

	public SingleAttributeFilter(Stream<String> path, FilterOperator operator) {
		super(path, operator, null);
		this.checkOperatorsWithoutValue(operator);
	}

	public SingleAttributeFilter(String path, FilterOperator operator) {
		super(path, operator, null);
		this.checkOperatorsWithoutValue(operator);
	}

	private void checkOperatorsWithValue(FilterOperator operator, T value) {
		if (!FilterOperator.singleAttributeFiltersWithValue.contains(operator)) {
			throw new FilterParameterException(String.format("Operator %s not allowed!", operator));
		}

		if (FilterOperator.singleComparableAttributeFilters.contains(operator) && !(value instanceof Comparable)) {
			throw new FilterParameterException(
					String.format("Operator %s not allowed! %s must implement %s", operator, value.getClass(),
							Comparable.class.getName()));
		}
	}

	private void checkOperatorsWithoutValue(FilterOperator operator) {
		if (!FilterOperator.singleAttributeFiltersWithNoValue.contains(operator)) {
			throw new FilterParameterException(String.format("Operator %s not allowed!", operator));
		}
	}

}
