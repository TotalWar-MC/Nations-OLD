package com.steffbeard.totalwar.nations.exceptions;

import com.steffbeard.totalwar.nations.config.Settings;

public class NotRegisteredException extends NationsException {

	private static final long serialVersionUID = 175945283391669005L;

	public NotRegisteredException() {

		super(Settings.getLangString("not_registered"));
	}

	public NotRegisteredException(String message) {

		super(message);
	}
}
