package com.steffbeard.totalwar.nations.war.siege.actions;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import com.steffbeard.totalwar.nations.NationsUniverse;
import com.steffbeard.totalwar.nations.config.Messages;
import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.exceptions.NationsException;
import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.objects.town.Town;
import com.steffbeard.totalwar.nations.permissions.PermissionNodes;
import com.steffbeard.totalwar.nations.util.TimeMgmt;
import com.steffbeard.totalwar.nations.war.siege.SiegeStatus;
import com.steffbeard.totalwar.nations.war.siege.location.Siege;
import com.steffbeard.totalwar.nations.war.siege.location.SiegeZone;

/**
 * This class is responsible for processing requests to Abandon siege attacks
 *
 * @author Goosius
 */
public class AbandonAttack {

	/**
	 * Process an abandon attack request
	 *
	 * This method does some final checks and if they pass, the abandon is executed
	 *
	 * @param player the player who placed the abandon banner
	 * @param siegeZone the siege zone
	 * @param event the place block event
	 */
    public static void processAbandonSiegeRequest(Player player, 
												  SiegeZone siegeZone,
												  BlockPlaceEvent event)  {
        try {
			NationsUniverse universe = NationsUniverse.getInstance();
            Resident resident = universe.getDataSource().getResident(player.getName());
            if(!resident.hasTown())
				throw new NationsException(Settings.getLangString("msg_err_siege_war_action_not_a_town_member"));

            Town townOfResident = resident.getTown();
            if(!townOfResident.hasNation())
				throw new NationsException(Settings.getLangString("msg_err_siege_war_action_not_a_nation_member"));

            //If player has no permission to abandon,send error
            if (!universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_NATION_SIEGE_ABANDON.getNode()))
                throw new NationsException(Settings.getLangString("msg_err_command_disable"));
            
            //If the siege is not in progress, send error
			Siege siege = siegeZone.getSiege();
			if (siege.getStatus() != SiegeStatus.IN_PROGRESS)
				throw new NationsException(Settings.getLangString("msg_err_siege_war_cannot_abandon_siege_over"));
			
			//If the player's nation does not own the nearby siegezone, send error
            if(siegeZone.getAttackingNation() != townOfResident.getNation())
                throw new NationsException(Settings.getLangString("msg_err_siege_war_cannot_abandon_nation_not_attacking_zone"));

			long timeUntilAbandonIsAllowedMillis = siege.getTimeUntilAbandonIsAllowedMillis();
			if(timeUntilAbandonIsAllowedMillis > 0) {
				String message = String.format(Settings.getLangString("msg_err_siege_war_cannot_abandon_yet"),
					TimeMgmt.getFormattedTimeValue(timeUntilAbandonIsAllowedMillis));
				throw new NationsException(message);
			}

			attackerAbandon(siegeZone);

        } catch (NationsException x) {
            Messages.sendErrorMsg(player, x.getMessage());
			event.setBuild(false);
            event.setCancelled(true);
        }
    }

    private static void attackerAbandon(SiegeZone siegeZone) {
        //Here we simply remove the siege zone
		NationsUniverse universe = NationsUniverse.getInstance();
		universe.getDataSource().removeSiegeZone(siegeZone);
        
		Messages.sendGlobalMessage(
			String.format(Settings.getLangString("msg_siege_war_attacker_abandon"),
				siegeZone.getAttackingNation().getFormattedName(),
        		siegeZone.getDefendingTown().getFormattedName()));
		
        if (siegeZone.getSiege().getSiegeZones().size() == 0) {
            SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siegeZone.getSiege(),
                    SiegeStatus.ATTACKER_ABANDON,
                    null);
			Messages.sendGlobalMessage(
				String.format(Settings.getLangString("msg_siege_war_siege_abandon"),
					siegeZone.getDefendingTown().getFormattedName()));
		}

		SiegeWarMoneyUtil.giveOneWarChestToWinnerTown(siegeZone, siegeZone.getDefendingTown());
    }
}
