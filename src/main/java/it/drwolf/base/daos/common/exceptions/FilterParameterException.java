package it.drwolf.base.daos.common.exceptions;

public class FilterParameterException extends RuntimeException {

	private static final long serialVersionUID = -1333740512833821895L;

	public FilterParameterException() {
		super();
	}

	public FilterParameterException(String message) {
		super(message);
	}

	public FilterParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	public FilterParameterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FilterParameterException(Throwable cause) {
		super(cause);
	}

}
