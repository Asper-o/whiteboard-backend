package com.aspero.collaborativeboard.core.exception;

// We extend RuntimeException so we don't have to add "throws" to every method signature
public class UserAlreadyExistsException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserAlreadyExistsException(String message) {
        super(message);
    }
}