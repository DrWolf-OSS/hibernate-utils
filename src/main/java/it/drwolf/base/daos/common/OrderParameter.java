package it.drwolf.base.daos.common;

/**
 * Parameter object that should be used in DAO's methods that return sorted
 * results
 *
 * @author spaladini
 *
 */
public class OrderParameter {

	public enum OrderType {
		ASC, DESC;
	}

	private final String orderField;

	private final OrderType orderType;

	public OrderParameter(String orderColumn, OrderType orderType) {

		if (orderColumn == null || orderColumn.trim().isEmpty()) {
			throw new IllegalArgumentException("orderColumn can't be null");
		}
		this.orderField = orderColumn;

		if (orderType != null) {
			this.orderType = orderType;
		} else {
			this.orderType = OrderType.ASC;
		}

	}

	/**
	 *
	 * Return the sorting field, it can't be NULL
	 *
	 * @return
	 */
	public String getOrderField() {
		return this.orderField;
	}

	/**
	 *
	 * Return the order type: ASC | DESC.<br>
	 * It can't be NULL, ASC is the default
	 *
	 * @return
	 */
	public OrderType getOrderType() {
		return this.orderType;
	}

}
