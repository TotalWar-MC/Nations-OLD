package com.steffbeard.totalwar.nations.invites;

import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;

import java.util.List;

/**
 * @author Articdive
 */
public interface NationsInviteReceiver {

	String getName();

	List<Invite> getReceivedInvites();

	void newReceivedInvite(Invite invite) throws TooManyInvitesException;

	void deleteReceivedInvite(Invite invite);
}