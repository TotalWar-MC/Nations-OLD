package com.steffbeard.totalwar.nations.invites;

import com.palmergames.bukkit.towny.exceptions.TownyException;

/**
 * @author Articdive
 * 
 * ripped off from Towny github, sue me
 * (actually no please dont)
 */
public interface Invite {

	/**
	 * @return - Playername of who sent the invite or null (Console).
	 */
	String getDirectSender();

	/**
	 * @return - Resident, Town or Nation as a TownyEconomyObject.
	 */
	NationsInviteReceiver getReceiver();

	/**
	 * @return - Resident, Town or Nation as TownyEconomyObject.
	 */
	NationsInviteSender getSender();

	/**
	 * @throws TownyException - Sends errors back up to be processed by the caller.
	 */
	void accept() throws TownyException;

	/**
	 * @param fromSender - Tells if invite was revoked (true) or declined (false).
	 */
	void decline(boolean fromSender);
}