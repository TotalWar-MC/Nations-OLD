package com.steffbeard.totalwar.nations.invites;

import com.steffbeard.totalwar.nations.exceptions.NationsException;

/**
 * @author Articdive
 */
public interface Invite {

	/**
	 * @return - Playername of who sent the invite or null (Console).
	 */
	String getDirectSender();

	/**
	 * @return - Resident, Town or Nation as a NationsEconomyObject.
	 */
	NationsInviteReceiver getReceiver();

	/**
	 * @return - Resident, Town or Nation as NationsEconomyObject.
	 */
	NationsInviteSender getSender();

	/**
	 * @throws NationsException - Sends errors back up to be processed by the caller.
	 */
	void accept() throws NationsException;

	/**
	 * @param fromSender - Tells if invite was revoked (true) or declined (false).
	 */
	void decline(boolean fromSender);
}
