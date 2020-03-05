package com.steffbeard.totalwar.nations.exceptions;

import com.steffbeard.totalwar.nations.objects.Alliance;

public class EmptyAllianceException extends Exception {

	private static final long serialVersionUID = 6093696939107516795L;
	private Alliance alliance;

	public EmptyAllianceException(Alliance alliance) {

		this.setAlliance(alliance);
	}

	public void setAlliance(Alliance alliance) {

		this.alliance = alliance;
	}

	public Alliance getAlliance() {

		return alliance;
	}
}
