package it.drwolf.base.daos.common;

import java.util.Collection;

/**
 *
 * Utility class useful to standardise the management of paginated data.<br>
 * All DAO's methods that involve paginated data should return an appropriate
 * instance of PaginatedData
 *
 * @author spaladini
 *
 * @param <T>
 */
public class PaginatedData<T> {

	private final Collection<T> elements;

	private final Integer page;

	private final Integer size;

	private final Integer total;

	public PaginatedData(Collection<T> elements, Integer page, Integer size, Integer total) {
		this.elements = elements;
		this.page = page;
		this.size = size;
		this.total = total;
	}

	/**
	 *
	 * Return the current page's elements
	 *
	 * @return
	 */
	public Collection<T> getElements() {
		return this.elements;
	}

	/**
	 *
	 * Return the number of the current page
	 *
	 * @return
	 */
	public Integer getPage() {
		return this.page;
	}

	/**
	 *
	 * Return the size of the current page
	 *
	 * @return
	 */
	public Integer getSize() {
		return this.size;
	}

	/**
	 *
	 * Return the total count of the results
	 *
	 * @return
	 */
	public Integer getTotal() {
		return this.total;
	}

}
