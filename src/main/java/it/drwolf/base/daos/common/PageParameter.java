package it.drwolf.base.daos.common;

/**
 *
 * Parameter object that should be used in DAO's methods that return paginated
 * results
 *
 * @author spaladini
 *
 */
public class PageParameter {

	private final Integer pageNumber;
	private final Integer size;

	public PageParameter(Integer pageNumber, Integer size) {

		if (pageNumber == null) {
			throw new IllegalArgumentException("Page number can't be null");
		}
		this.pageNumber = pageNumber;

		if (size == null) {
			throw new IllegalArgumentException("Size can't be null");
		}
		this.size = size;

	}

	public Integer getPageNumber() {
		return this.pageNumber;
	}

	public Integer getSize() {
		return this.size;
	}

}
