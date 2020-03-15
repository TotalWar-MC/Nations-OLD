package com.steffbeard.totalwar.nations.exceptions;

public class NationsException extends Exception {

	private static final long serialVersionUID = -6821768221748544277L;

	@Deprecated
	public String getError() {

		return getMessage();
	}

	public NationsException() {

		super("unknown");
	}

	public NationsException(String message) {

		super(message);
	}
}
