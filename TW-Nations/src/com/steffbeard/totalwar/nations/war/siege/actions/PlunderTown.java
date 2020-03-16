package com.steffbeard.totalwar.nations.war.siege.actions;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import com.steffbeard.totalwar.nations.NationsUniverse;
import com.steffbeard.totalwar.nations.config.Messages;
import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.economy.NationsEconomyHandler;
import com.steffbeard.totalwar.nations.exceptions.EconomyException;
import com.steffbeard.totalwar.nations.exceptions.NationsException;
import com.steffbeard.totalwar.nations.objects.nations.Nation;
import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.objects.town.Town;
import com.steffbeard.totalwar.nations.permissions.PermissionNodes;
import com.steffbeard.totalwar.nations.war.siege.SiegeStatus;
import com.steffbeard.totalwar.nations.war.siege.location.Siege;

/**
 * This class is responsible for processing requests to plunder towns
 *
 * @author Goosius
 */
public class PlunderTown {

	/**
	 * Process a plunder town request
	 *
	 * This method does some final checks and if they pass, the plunder is executed.
	 *
	 * @param player the player who placed the plunder chest
	 * @param townToBePlundered the town to be plundered
	 * @param event the place block event
	 */
    public static void processPlunderTownRequest(Player player,
												 Town townToBePlundered,
												 BlockPlaceEvent event) {
        try {
			if(!Settings.isUsingEconomy())
				throw new NationsException(Settings.getLangString("msg_err_siege_war_cannot_plunder_without_economy"));

			if(Settings.getWarSiegeTownNeutralityEnabled() && townToBePlundered.isNeutral())
				throw new NationsException(Settings.getLangString("msg_err_siege_war_neutral_town_cannot_plunder"));
			
			NationsUniverse universe = NationsUniverse.getInstance();
			Resident resident = universe.getDataSource().getResident(player.getName());
			if(!resident.hasTown())
				throw new NationsException(Settings.getLangString("msg_err_siege_war_action_not_a_town_member"));

			Town townOfPlunderingResident = resident.getTown();
			if(!townOfPlunderingResident.hasNation())
				throw new NationsException(Settings.getLangString("msg_err_siege_war_action_not_a_nation_member"));
			
			if (!universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_NATION_SIEGE_PLUNDER.getNode()))
                throw new NationsException(Settings.getLangString("msg_err_command_disable"));

			if(townOfPlunderingResident == townToBePlundered)
				throw new NationsException(Settings.getLangString("msg_err_siege_war_cannot_plunder_own_town"));

			Siege siege = townToBePlundered.getSiege();
			if (siege.getStatus() != SiegeStatus.ATTACKER_WIN && siege.getStatus() != SiegeStatus.DEFENDER_SURRENDER)
				throw new NationsException(Settings.getLangString("msg_err_siege_war_cannot_plunder_without_victory"));
			
			if(townOfPlunderingResident.getNation() != siege.getAttackerWinner())
				throw new NationsException(Settings.getLangString("msg_err_siege_war_cannot_plunder_without_victory"));
			
            if(siege.isTownPlundered())
                throw new NationsException(String.format(Settings.getLangString("msg_err_siege_war_town_already_plundered"), townToBePlundered.getName()));

            plunderTown(siege, townToBePlundered, siege.getAttackerWinner(), event);
            
        } catch (NationsException x) {
            event.setBuild(false);
        	event.setCancelled(true);
            Messages.sendErrorMsg(player, x.getMessage());
        }
    }

    private static void plunderTown(Siege siege, Town defendingTown, Nation winnerNation, BlockPlaceEvent event) {
        siege.setTownPlundered(true);

        double fullPlunderAmount =
                Settings.getWarSiegeAttackerPlunderAmountPerPlot()
                        * defendingTown.getTownBlocks().size();
        try {
			NationsUniverse universe = NationsUniverse.getInstance();
			
			if (defendingTown.getAccount().canPayFromHoldings(fullPlunderAmount)) {
                defendingTown.getAccount().payTo(fullPlunderAmount, winnerNation, "Town was plundered by attacker");
                sendPlunderSuccessMessage(defendingTown, winnerNation, fullPlunderAmount);
				universe.getDataSource().saveTown(defendingTown);
            } else {
                double actualPlunderAmount = defendingTown.getAccount().getHoldingBalance();
                defendingTown.getAccount().payTo(actualPlunderAmount, winnerNation, "Town was plundered by attacker");
                sendPlunderSuccessMessage(defendingTown, winnerNation, actualPlunderAmount);
                Messages.sendGlobalMessage(
                	String.format(
						Settings.getLangString("msg_siege_war_town_ruined_from_plunder"),
						defendingTown.getFormattedName(),
						winnerNation.getFormattedName()));
				universe.getDataSource().removeTown(defendingTown);
            }
        } catch (EconomyException x) {
			event.setBuild(false);
			event.setCancelled(true);
            Messages.sendErrorMsg(x.getMessage());
        }
    }

    private static void sendPlunderSuccessMessage(Town defendingTown, Nation winnerNation, double plunderAmount) {
        //Same messages for now but may diverge in future (if we decide to track the original nation of the town)
    	if(defendingTown.hasNation()) {
			Messages.sendGlobalMessage(String.format(
					Settings.getLangString("msg_siege_war_nation_town_plundered"),
					defendingTown.getFormattedName(),
					NationsEconomyHandler.getFormattedBalance(plunderAmount),
					winnerNation.getFormattedName()
			));
        } else {
            Messages.sendGlobalMessage(String.format(
                    Settings.getLangString("msg_siege_war_neutral_town_plundered"),
                    defendingTown.getFormattedName(),
   				    NationsEconomyHandler.getFormattedBalance(plunderAmount),
                    winnerNation.getFormattedName()
			));
        }
    }

}
