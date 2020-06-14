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

	/**
	 * page elements
	 */
	private Collection<T> elements;

	/**
	 * current page
	 */
	private Integer page;

	/**
	 * page size
	 */
	private Integer size;

	/**
	 * total results
	 */
	private Integer total;

	public PaginatedData() {
	}

	public PaginatedData(Collection<T> elements, Integer page, Integer size, Integer total) {
		this.elements = elements;
		this.page = page;
		this.size = size;
		this.total = total;
	}

	public Collection<T> getElements() {
		return this.elements;
	}

	public Integer getPage() {
		return this.page;
	}

	public Integer getSize() {
		return this.size;
	}

	public Integer getTotal() {
		return this.total;
	}

	public void setElements(Collection<T> elements) {
		this.elements = elements;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

}
