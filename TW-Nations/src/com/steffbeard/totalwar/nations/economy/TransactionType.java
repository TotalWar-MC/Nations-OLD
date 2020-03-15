package com.steffbeard.totalwar.nations.economy;

public enum TransactionType {
	DEPOSIT("Deposit"), WITHDRAW("Withdraw"), ADD("Add"), SUBTRACT("Subtract");
	
	private String name;
	
	TransactionType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
