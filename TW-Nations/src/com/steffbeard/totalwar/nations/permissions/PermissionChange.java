package com.steffbeard.totalwar.nations.permissions;

/**
 * A class that represents a permission change to a town block owner.
 * This class can be used to cache a permission change and apply it to multiple town block owners efficiently.
 */
public class PermissionChange {

	// Enum represents a permission change action
	public enum Action {
		ALL_PERMS, SINGLE_PERM, PERM_LEVEL, ACTION_TYPE, RESET
	}

	private Object[] args;
	private Action changeAction;
	private boolean changeValue;

	public PermissionChange(Action changeAction, boolean changeValue, Object... args) {
		this.changeAction = changeAction;
		this.changeValue = changeValue;
		this.args = args;
	}
	
	public Action getChangeAction() {
		return changeAction;
	}
	
	public boolean getChangeValue() {
		return changeValue;
	}

	public Object[] getArgs() {
		return args;
	}
}
