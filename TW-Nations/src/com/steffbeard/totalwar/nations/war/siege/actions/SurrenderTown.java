package com.steffbeard.totalwar.nations.war.siege.actions;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import com.steffbeard.totalwar.nations.NationsUniverse;
import com.steffbeard.totalwar.nations.config.Messages;
import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.exceptions.NationsException;
import com.steffbeard.totalwar.nations.objects.nations.Nation;
import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.objects.town.Town;
import com.steffbeard.totalwar.nations.permissions.PermissionNodes;
import com.steffbeard.totalwar.nations.util.time.TimeMgmt;
import com.steffbeard.totalwar.nations.war.siege.SiegeStatus;
import com.steffbeard.totalwar.nations.war.siege.location.Siege;

import static com.steffbeard.totalwar.nations.util.time.TimeMgmt.ONE_HOUR_IN_MILLIS;

import java.util.ArrayList;

/**
 * This class is responsible for processing requests to surrender towns
 *
 * @author Goosius
 */
public class SurrenderTown {

	/**
	 * Process a surrender town request
	 * 
	 * This method does some final checks and if they pass, the surrender is executed.
	 * 
	 * @param player the player who placed the surrender banner
	 * @param townWhereBlockWasPlaced the town where the banner was placed
	 * @param event the place block event
	 */
    public static void processTownSurrenderRequest(Player player,
                                                   Town townWhereBlockWasPlaced,
                                                   BlockPlaceEvent event) {
        try {
			NationsUniverse universe = NationsUniverse.getInstance();
			Resident resident = universe.getDataSource().getResident(player.getName());
            if(!resident.hasTown())
				throw new NationsException(Settings.getLangString("msg_err_siege_war_action_not_a_town_member"));

			Town townOfAttackingResident = resident.getTown();
			if(townOfAttackingResident != townWhereBlockWasPlaced)
                throw new NationsException(Settings.getLangString("msg_err_siege_war_cannot_surrender_not_your_town"));
			
			if (!universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_TOWN_SIEGE_SURRENDER.getNode()))
                throw new NationsException(Settings.getLangString("msg_err_command_disable"));

            Siege siege = townWhereBlockWasPlaced.getSiege();
            if(siege.getStatus() != SiegeStatus.IN_PROGRESS)
				throw new NationsException(Settings.getLangString("msg_err_siege_war_cannot_surrender_siege_finished"));

            if(siege.getSiegeZones().size() > 1)
                throw new NationsException(Settings.getLangString("msg_err_siege_war_cannot_surrender_multiple_attackers"));

            long timeUntilSurrenderIsAllowedMillis = siege.getTimeUntilSurrenderIsAllowedMillis();
            if(timeUntilSurrenderIsAllowedMillis > 0) {
				String message = String.format(Settings.getLangString("msg_err_siege_war_cannot_surrender_yet"), 
					TimeMgmt.getFormattedTimeValue(timeUntilSurrenderIsAllowedMillis));
				throw new NationsException(message);
			}
            
            //Surrender
            defenderSurrender(siege);

        } catch (NationsException x) {
            Messages.sendErrorMsg(player, x.getMessage());
			event.setBuild(false);
			event.setCancelled(true);
        }
    }

    private static void defenderSurrender(Siege siege) {
    	Nation winnerNation = new ArrayList<>(siege.getSiegeZones().keySet()).get(0);
    	
        SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siege,
                                            SiegeStatus.DEFENDER_SURRENDER,
											winnerNation);

        Messages.sendGlobalMessage(String.format(
        	Settings.getLangString("msg_siege_war_town_surrender"),
			siege.getDefendingTown().getFormattedName(),
			siege.getAttackerWinner().getFormattedName()));

		SiegeWarMoneyUtil.giveWarChestsToWinnerNation(siege, siege.getAttackerWinner());
    }
}
