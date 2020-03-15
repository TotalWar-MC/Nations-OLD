package com.steffbeard.totalwar.nations.invites;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;

import com.steffbeard.totalwar.nations.Main;
import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.exceptions.NationsException;
import com.steffbeard.totalwar.nations.objects.nations.Nation;
import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.objects.town.Town;

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

	public static void acceptInvite(Invite invite) throws InvalidObjectException, NationsException {
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
			if (Settings.getMaximumRequestsSentNation() == 0){
				amount = 100;
			} else {
				amount = Settings.getMaximumRequestsSentNation();
			}
		}
		return amount;
	}

	public static int getReceivedInvitesMaxAmount(NationsInviteReceiver receiver) {

		int amount = 0;
		if (receiver instanceof Resident) {
			if (Settings.getMaximumInvitesReceivedResident() == 0) {
				amount = 100;
			} else {
				amount = Settings.getMaximumInvitesReceivedResident();
			}
		}
		if (receiver instanceof Town) {
			if (Settings.getMaximumInvitesReceivedTown() == 0) {
				amount = 100;
			} else {
				amount = Settings.getMaximumInvitesReceivedTown();
			}
		}
		if (receiver instanceof Nation) {
			if (Settings.getMaximumRequestsReceivedNation() == 0) {
				amount = 100;
			} else {
				amount = Settings.getMaximumRequestsReceivedNation();
			}
		}
		return amount;
	}

	public static int getSentInvitesMaxAmount(NationsInviteSender sender) {
		int amount = 0;
		if (sender instanceof Town) {
			if (Settings.getMaximumInvitesSentTown() == 0) {
				amount = 100;
			} else {
				amount = Settings.getMaximumInvitesSentTown();
			}
		}
		if (sender instanceof Nation) {
			if (Settings.getMaximumInvitesSentNation() == 0) {
				amount = 100;
			} else {
				amount = Settings.getMaximumInvitesSentNation();
			}
		}
		return amount;
	}

}
