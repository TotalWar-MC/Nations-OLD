package com.steffbeard.totalwar.nations.handlers;

import com.steffbeard.totalwar.nations.economy.EconomyAccount;

/**
 * An interface used to show that an object is capable of participating
 * in economy specific tasks.
 * 
 * @author Suneet Tipirneni (Siris)
 */
public interface EconomyHandler {
	/**
	 * Gets the {@link EconomyAccount} associated with this object.
	 * 
	 * @return An {@link EconomyAccount} for this class.
	 */
	EconomyAccount getAccount();
}
