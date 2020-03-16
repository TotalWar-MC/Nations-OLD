package com.steffbeard.totalwar.nations.war.siege.actions;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

import com.steffbeard.totalwar.nations.Main;
import com.steffbeard.totalwar.nations.NationsUniverse;
import com.steffbeard.totalwar.nations.config.Messages;
import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.exceptions.NationsException;
import com.steffbeard.totalwar.nations.exceptions.NotRegisteredException;
import com.steffbeard.totalwar.nations.objects.nations.Nation;
import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.objects.town.Town;
import com.steffbeard.totalwar.nations.permissions.PermissionNodes;
import com.steffbeard.totalwar.nations.util.coord.Coord;
import com.steffbeard.totalwar.nations.war.siege.SiegeStatus;
import com.steffbeard.totalwar.nations.war.siege.location.Siege;

/**
 * This class is responsible for processing requests to invade towns
 *
 * @author Goosius
 */
public class InvadeTown {

	/**
	 * Process an invade town request
	 *
	 * This method does some final checks and if they pass, the invasion is executed.
	 *
	 * @param plugin the town plugin object
	 * @param player the player who placed the invade banner
	 * @param townToBeInvaded the town to be invaded
	 * @param event the place block event
	 */
    public static void processInvadeTownRequest(Main plugin,
                                                Player player,
                                                Town townToBeInvaded,
                                                BlockPlaceEvent event) {
        try {
			NationsUniverse universe = NationsUniverse.getInstance();
			Resident resident = universe.getDataSource().getResident(player.getName());
			Town townOfInvadingResident = resident.getTown();

			if (!universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_NATION_SIEGE_INVADE.getNode()))
				throw new NationsException(Settings.getLangString("msg_err_command_disable"));

			if(townOfInvadingResident == townToBeInvaded)
				throw new NationsException(Settings.getLangString("msg_err_siege_war_cannot_invade_own_town"));

			Siege siege = townToBeInvaded.getSiege();
			if (siege.getStatus() != SiegeStatus.ATTACKER_WIN && siege.getStatus() != SiegeStatus.DEFENDER_SURRENDER)
				throw new NationsException(Settings.getLangString("msg_err_siege_war_cannot_invade_without_victory"));

			Nation nationOfInvadingResident = townOfInvadingResident.getNation();
			Nation attackerWinner = siege.getAttackerWinner();
			
			if (nationOfInvadingResident != attackerWinner)
				throw new NationsException(Settings.getLangString("msg_err_siege_war_cannot_invade_without_victory"));

            if (siege.isTownInvaded())
                throw new NationsException(String.format(Settings.getLangString("msg_err_siege_war_town_already_invaded"), townToBeInvaded.getName()));

			if(townToBeInvaded.hasNation() && townToBeInvaded.getNation() == attackerWinner)
				throw new NationsException(String.format(Settings.getLangString("msg_err_siege_war_town_already_belongs_to_your_nation"), townToBeInvaded.getName()));

			if (Settings.getNationRequiresProximity() > 0) {
				Coord capitalCoord = attackerWinner.getCapital().getHomeBlock().getCoord();
				Coord townCoord = townToBeInvaded.getHomeBlock().getCoord();
				if (!attackerWinner.getCapital().getHomeBlock().getWorld().getName().equals(townToBeInvaded.getHomeBlock().getWorld().getName())) {
					throw new NationsException(Settings.getLangString("msg_err_nation_homeblock_in_another_world"));
				}
				double distance;
				distance = Math.sqrt(Math.pow(capitalCoord.getX() - townCoord.getX(), 2) + Math.pow(capitalCoord.getZ() - townCoord.getZ(), 2));
				if (distance > Settings.getNationRequiresProximity()) {
					throw new NationsException(String.format(Settings.getLangString("msg_err_town_not_close_enough_to_nation"), townToBeInvaded.getName()));
				}
			}

			if (Settings.getMaxTownsPerNation() > 0) {
				if (attackerWinner.getTowns().size() >= Settings.getMaxTownsPerNation()){
					throw new NationsException(String.format(Settings.getLangString("msg_err_nation_over_town_limit"), Settings.getMaxTownsPerNation()));
				}
			}

			captureTown(plugin, siege, attackerWinner, townToBeInvaded);

        } catch (NationsException x) {
			event.setBuild(false);
			event.setCancelled(true);
            Messages.sendErrorMsg(player, x.getMessage());
        }
    }

    private static void captureTown(Main plugin, Siege siege, Nation attackingNation, Town defendingTown) {
		NationsUniverse universe = NationsUniverse.getInstance();

		siege.setTownInvaded(true);

        //Reset revolt immunity, to prevent immediate revolt after invasion 
        SiegeWarTimeUtil.activateRevoltImmunityTimer(defendingTown);
		
        if(defendingTown.hasNation()) {
            Nation nationOfDefendingTown = null;
            try {
                nationOfDefendingTown = defendingTown.getNation();
            } catch (NotRegisteredException x) {
            }

			//Remove town from nation (and nation itself if empty)
			universe.getDataSource().removeTownFromNation(plugin, defendingTown, nationOfDefendingTown);

            universe.getDataSource().addTownToNation(plugin, defendingTown, attackingNation);

            Messages.sendGlobalMessage(String.format(
                    Settings.getLangString("msg_siege_war_nation_town_captured"),
                    defendingTown.getFormattedName(),
                    nationOfDefendingTown.getFormattedName(),
                    attackingNation.getFormattedName()
            ));

            if(nationOfDefendingTown.getTowns().size() == 0) {
                Messages.sendGlobalMessage(String.format(
                        Settings.getLangString("msg_siege_war_nation_defeated"),
                        nationOfDefendingTown.getFormattedName()
                ));
            }
        } else {
            universe.getDataSource().addTownToNation(plugin, defendingTown, attackingNation);

            Messages.sendGlobalMessage(String.format(
                    Settings.getLangString("msg_siege_war_neutral_town_captured"),
                    defendingTown.getFormattedName(),
                    attackingNation.getFormattedName()
            ));
        }

		defendingTown.setOccupied(true);

		//Save the town to ensure data is saved even if only town/siege was updated
		NationsUniverse.getInstance().getDataSource().saveTown(defendingTown);
    }
}
