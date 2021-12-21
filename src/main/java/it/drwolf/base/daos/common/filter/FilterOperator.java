package it.drwolf.base.daos.common.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum FilterOperator {
	EQ, GT, GE, LT, LE, LIKE, NOT_LIKE, IS_TRUE, IS_FALSE, IN, NOT_IN, IS_NULL, IS_NOT_NULL, IS_EMPTY;

	static final Set<FilterOperator> singleAttributeFiltersWithValue = new HashSet<>(
			Arrays.asList(FilterOperator.EQ, FilterOperator.GT, FilterOperator.GE, FilterOperator.LT, FilterOperator.LE,
					FilterOperator.LIKE, FilterOperator.NOT_LIKE, FilterOperator.IS_TRUE, FilterOperator.IS_FALSE));

	static final Set<FilterOperator> singleComparableAttributeFilters = new HashSet<>(
			Arrays.asList(FilterOperator.GT, FilterOperator.GE, FilterOperator.LT, FilterOperator.LE));

	static final Set<FilterOperator> singleAttributeFiltersWithNoValue = new HashSet<>(
			Arrays.asList(FilterOperator.IS_NULL, FilterOperator.IS_NOT_NULL, FilterOperator.IS_TRUE,
					FilterOperator.IS_FALSE));

	static final Set<FilterOperator> collectionAttributeFiltersWithValue = new HashSet<>(
			Arrays.asList(FilterOperator.EQ, FilterOperator.IN, FilterOperator.NOT_IN, FilterOperator.IS_NULL,
					FilterOperator.IS_NOT_NULL, FilterOperator.IS_EMPTY));

	static final Set<FilterOperator> collectionAttributeFiltersWithNoValue = new HashSet<>(
			Arrays.asList(FilterOperator.IS_NULL, FilterOperator.IS_NOT_NULL));
}
