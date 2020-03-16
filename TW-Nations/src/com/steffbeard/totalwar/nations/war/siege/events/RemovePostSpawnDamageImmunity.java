package com.steffbeard.totalwar.nations.war.siege.events;

import org.bukkit.entity.Player;

import com.steffbeard.totalwar.nations.NationsUniverse;
import com.steffbeard.totalwar.nations.config.Messages;
import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.util.BukkitTools;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * This class is responsible for removing post spawn damage immunity
 *
 * @author Goosius
 */
public class RemovePostSpawnDamageImmunity {

	/**
	 * This method cycles through all online players
	 * It determines which players are currently damage immune, but have reached the immunity time limit - then removes the immunity
	 */
    public static void removePostSpawnDamageImmunity() {
		NationsUniverse universe = NationsUniverse.getInstance();
		List<Player> onlinePlayers = new ArrayList<>(BukkitTools.getOnlinePlayers());
		ListIterator<Player> playerItr = onlinePlayers.listIterator();
		Player player;
		Resident resident;

		while (playerItr.hasNext()) {
			player = playerItr.next();
			/*
			 * We are running in an Async thread so MUST verify all objects.
			 */
			try {
				if(player.isOnline() && player.isInvulnerable()) {
					resident = universe.getDataSource().getResident(player.getName());
					if(System.currentTimeMillis() > resident.getDamageImmunityEndTime()) {
						player.setInvulnerable(false);
					}
				}
			} catch (Exception e) {
				Messages.sendErrorMsg("Problem removing immunity from player " + player.getName());
				e.printStackTrace();
			}
		}
    }

}
