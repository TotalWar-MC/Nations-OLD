package com.steffbeard.totalwar.nations.objects;

import com.steffbeard.totalwar.nations.exceptions.EconomyException;
import com.steffbeard.totalwar.nations.exceptions.NationsException;
import com.steffbeard.totalwar.nations.handlers.EconomyHandler;
import com.steffbeard.totalwar.nations.objects.resident.Resident;

/**
 * Allows objects to manage a self contained bank.
 */
public interface Bank extends EconomyHandler {
	/**
	 * Takes money from object bank account and gives it to a resident.
	 * 
	 * @param resident The resident to pay to.
	 * @param amount The amount to pay.
	 * @throws EconomyException Thrown if there is an economy error.
	 * @throws TownyException Thrown if their is a hierarchical error.
	 */
	void withdrawFromBank(Resident resident, int amount) throws EconomyException, NationsException;
}
