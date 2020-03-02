package com.steffbeard.totalwar.nations.invites;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Town;
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

	public static int getSentAllyRequestsAmount(Nation sender) {
		List<Invite> invites = sender.getSentAllyInvites();
		return invites.size();
	}

	public static int getSentAllyRequestsMaxAmount(Nation sender) {
		int amount = 0;
		if (sender != null) {
			if (NationsSettings.getMaximumRequestsSentNation() == 0){
				amount = 100;
			} else {
				amount = NationsSettings.getMaximumRequestsSentNation();
			}
		}
		return amount;
	}

	public static int getReceivedInvitesMaxAmount(NationsInviteReceiver receiver) {

		int amount = 0;
		if (receiver instanceof Resident) {
			if (NationsSettings.getMaximumInvitesReceivedResident() == 0) {
				amount = 100;
			} else {
				amount = NationsSettings.getMaximumInvitesReceivedResident();
			}
		}
		if (receiver instanceof Town) {
			if (NationsSettings.getMaximumInvitesReceivedTown() == 0) {
				amount = 100;
			} else {
				amount = NationsSettings.getMaximumInvitesReceivedTown();
			}
		}
		if (receiver instanceof Nation) {
			if (NationsSettings.getMaximumRequestsReceivedNation() == 0) {
				amount = 100;
			} else {
				amount = NationsSettings.getMaximumRequestsReceivedNation();
			}
		}
		return amount;
	}

	public static int getSentInvitesMaxAmount(NationsInviteSender sender) {
		int amount = 0;
		if (sender instanceof Town) {
			if (NationsSettings.getMaximumInvitesSentTown() == 0) {
				amount = 100;
			} else {
				amount = NationsSettings.getMaximumInvitesSentTown();
			}
		}
		if (sender instanceof Nation) {
			if (NationsSettings.getMaximumInvitesSentNation() == 0) {
				amount = 100;
			} else {
				amount = NationsSettings.getMaximumInvitesSentNation();
			}
		}
		return amount;
	}

}