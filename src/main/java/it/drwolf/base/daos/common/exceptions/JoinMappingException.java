package it.drwolf.base.daos.common.exceptions;

public class JoinMappingException extends RuntimeException {

	public JoinMappingException() {
		super();
	}

	public JoinMappingException(String message) {
		super(message);
	}

	public JoinMappingException(String message, Throwable cause) {
		super(message, cause);
	}
}
