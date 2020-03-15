package com.steffbeard.totalwar.nations.invites;

import java.util.List;

import com.steffbeard.totalwar.nations.exceptions.TooManyInvitesException;
import com.steffbeard.totalwar.nations.objects.Nameable;

/**
 * @author Articdive
 */
public interface NationsInviteSender extends Nameable {

	List<Invite> getSentInvites();

	void newSentInvite(Invite invite) throws TooManyInvitesException;

	void deleteSentInvite(Invite invite);
}
