package com.steffbeard.totalwar.nations.invites;

import java.util.List;

import com.steffbeard.totalwar.nations.exceptions.TooManyInvitesException;

/**
 * @author Articdive
 */
public interface NationsInviteReceiver {

	String getName();

	List<Invite> getReceivedInvites();

	void newReceivedInvite(Invite invite) throws TooManyInvitesException;

	void deleteReceivedInvite(Invite invite);
}
