package it.drwolf.base.daos.common.filter;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FiltersUtils {

	private FiltersUtils() {
	}

	public static <T> void addSingleAttributeFilter(Set<FilterParameter> filters, String path, FilterOperator operator,
			T value) {

		if (value instanceof String) {
			String stringValue = (String) value;
			if (stringValue != null && !stringValue.trim().isEmpty()) {
				FilterParameter<String> stringFilterParameter = new SingleAttributeFilter<>(path, operator,
						stringValue.trim());
				filters.add(stringFilterParameter);
			}
		} else {
			if (value != null) {
				FilterParameter<T> filterParameter = new SingleAttributeFilter<>(path, operator, value);
				filters.add(filterParameter);
			}
		}
	}

	public static <T> void addSingleAttributeFilter(Set<FilterParameter> filters, Stream<String> pathAsStream,
			FilterOperator operator, T value) {
		FiltersUtils.addSingleAttributeFilter(filters, pathAsStream.collect(Collectors.joining(".")), operator, value);
	}

	public static <T extends Collection> void addCollectionAttributeFilter(Set<FilterParameter> filters, String path,
			FilterOperator operator, T value) {
		if (value != null && !value.isEmpty()) {
			FilterParameter<T> statusFilter = new CollectionAttributeFilter<>(path, operator, value);
			filters.add(statusFilter);
		}
	}

	public static <T extends Collection> void addCollectionAttributeFilter(Set<FilterParameter> filters,
			Stream<String> pathAsStream, FilterOperator operator, T value) {
		FiltersUtils.addCollectionAttributeFilter(filters, pathAsStream.collect(Collectors.joining(".")), operator,
				value);
	}
}
