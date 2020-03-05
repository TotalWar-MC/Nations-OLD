package com.steffbeard.totalwar.nations.invites;

import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;
import com.steffbeard.totalwar.nations.objects.Nameable;

import java.util.List;

/**
 * @author Articdive
 */
public interface NationsInviteSender extends Nameable {

	List<Invite> getSentInvites();

	void newSentInvite(Invite invite) throws TooManyInvitesException;

	void deleteSentInvite(Invite invite);
}