package com.steffbeard.totalwar.nations.invites;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.steffbeard.totalwar.nations.Main;

/**
 * @author - Articdive
 */
public class InviteHandler {
	@SuppressWarnings("unused")
	private static Main plugin;
	
	private static List<Invite> activeInvites = new ArrayList<>();

	public static void initialize(Main plugin) {

		InviteHandler.plugin = plugin;
	}

	public static void acceptInvite(Invite invite) throws InvalidObjectException, TownyException {
		if (activeInvites.contains(invite)) {
			invite.accept();
			activeInvites.remove(invite);
			return;
		}
		throw new InvalidObjectException("Invite not valid!"); // I throw this as a backup (failsafe)
		// It shouldn't be possible for this exception to happen via normally using Nations
	}

	public static void declineInvite(Invite invite, boolean fromSender) throws InvalidObjectException {
		if (activeInvites.contains(invite)) {
			invite.decline(fromSender);
			activeInvites.remove(invite);
			return;
		}
		throw new InvalidObjectException("Invite not valid!"); // I throw this as a backup (failsafe)
		// It shouldn't be possible for this exception to happen via normally using Nations
	}
	
	public static void addInvite(Invite invite) {
		activeInvites.add(invite);
	}
	
	public static List<Invite> getActiveInvites() {
		return activeInvites;
	}
	
	public static boolean inviteIsActive(Invite invite) {
		for (Invite activeInvite : activeInvites) {
			if (activeInvite.getReceiver().equals(invite.getReceiver()) && activeInvite.getSender().equals(invite.getSender()))
				return true;
		}
		return false;
	}
	
	public static boolean inviteIsActive(NationsInviteSender sender, NationsInviteReceiver receiver) {
		for (Invite activeInvite : activeInvites) {
			if (activeInvite.getReceiver().equals(receiver) && activeInvite.getSender().equals(sender))
				return true;
		}
		return false;
	}

	public static int getReceivedInvitesAmount(NationsInviteReceiver receiver) {
		List<Invite> invites = receiver.getReceivedInvites();
		return invites.size();
	}

	public static int getSentInvitesAmount(NationsInviteSender sender) {
		List<Invite> invites = sender.getSentInvites();
		return invites.size();
	}
}