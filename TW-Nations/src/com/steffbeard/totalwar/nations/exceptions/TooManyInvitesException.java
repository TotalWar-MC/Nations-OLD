package com.steffbeard.totalwar.nations.exceptions;

@SuppressWarnings("serial")
public class TooManyInvitesException extends Exception {

	@Deprecated
	public String getError() {

		return getMessage();
	}

	public TooManyInvitesException() {

		super("unknown");
	}

	public TooManyInvitesException(String message) {

		super(message);
	}
}
