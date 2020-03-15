package com.steffbeard.totalwar.nations.exceptions;

public class AlreadyRegisteredException extends NationsException {

	private static final long serialVersionUID = 4191685552690886161L;

	public AlreadyRegisteredException() {

		super("Already registered.");
	}

	public AlreadyRegisteredException(String message) {

		super(message);
	}
}
