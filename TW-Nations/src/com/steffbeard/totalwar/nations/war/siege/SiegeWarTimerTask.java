package com.steffbeard.totalwar.nations.war.siege;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.steffbeard.totalwar.nations.Main;
import com.steffbeard.totalwar.nations.NationsUniverse;
import com.steffbeard.totalwar.nations.config.Messages;
import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.exceptions.NotRegisteredException;
import com.steffbeard.totalwar.nations.objects.NationsObject;
import com.steffbeard.totalwar.nations.objects.nations.Nation;
import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.objects.town.Town;
import com.steffbeard.totalwar.nations.permissions.PermissionNodes;
import com.steffbeard.totalwar.nations.util.BukkitTools;
import com.steffbeard.totalwar.nations.war.siege.events.AttackerWin;
import com.steffbeard.totalwar.nations.war.siege.events.DefenderWin;
import com.steffbeard.totalwar.nations.war.siege.events.RemovePostSpawnDamageImmunity;
import com.steffbeard.totalwar.nations.war.siege.events.RemoveRuinedTowns;
import com.steffbeard.totalwar.nations.war.siege.location.Siege;
import com.steffbeard.totalwar.nations.war.siege.location.SiegeZone;

import static com.steffbeard.totalwar.nations.util.time.TimeMgmt.ONE_MINUTE_IN_MILLIS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents the siegewar timer task
 * 
 * The task is recommended to run about once every 10 seconds. 
 * This rate can be configured.
 * 
 * The task does 3 things when it runs:
 * 1. Evaluate each siege zone, and adjust siege points depending on which players are in the zones.
 * 2. Evaluate each siege, to determine if the main phase is completed, or the aftermath is completed.
 * 3. Completely delete towns which have already been ruined for a certain amount of time.
 * 
 * @author Goosius
 */
public class SiegeWarTimerTask extends NationsTimerTask {
	private long nextRuinsRemovalsTick;

	public SiegeWarTimerTask(Main plugin) {
		super(plugin);
		nextRuinsRemovalsTick = System.currentTimeMillis() + (long)(Settings.getWarSiegeRuinsRemovalsTickIntervalMinutes() * ONE_MINUTE_IN_MILLIS);
	}

	@Override
	public void run() {
		if (Settings.getWarSiegeEnabled()) {
			
			evaluateSiegeZones();

			evaluateSieges();

			evaluateRuinsRemovals();

			evaluatePostSpawnDamageImmunityRemovals();
		}
	}

	/**
	 * Evaluate all siege zones
	 */
	private void evaluateSiegeZones() {
		NationsUniverse universe = NationsUniverse.getInstance();
		for(SiegeZone siegeZone: universe.getDataSource().getSiegeZones()) {
			try {
				evaluateSiegeZone(siegeZone);
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Evaluate all sieges
	 */
	private void evaluateSieges() {
		for(Siege siege: getAllSieges()) {
			evaluateSiege(siege);
		}
	}

	/**
	 * Evaluate ruins removals
	 */
	public void evaluateRuinsRemovals() {
		if(Settings.getWarSiegeDelayFullTownRemoval() && System.currentTimeMillis() > nextRuinsRemovalsTick) {
			Messages.sendDebugMsg("Checking ruined towns now for deletion.");
			RemoveRuinedTowns.deleteRuinedTowns();
			nextRuinsRemovalsTick = System.currentTimeMillis() + (long)(Settings.getWarSiegeRuinsRemovalsTickIntervalMinutes() * ONE_MINUTE_IN_MILLIS);
		}
	}

	/**
	 * Evaluate post spawn damage immunity removals
	 */
	private void evaluatePostSpawnDamageImmunityRemovals() {
		if(Settings.getWarSiegePostSpawnDamageImmunityEnabled()) {
			RemovePostSpawnDamageImmunity.removePostSpawnDamageImmunity();
		}
	}

	/**
	 * Evaluate just 1 siege zone
	 */
	private static void evaluateSiegeZone(SiegeZone siegeZone) {
		boolean attackPointsAwarded;
		boolean defencePointsAwarded;

		int attackPointInstancesAwarded = 0;
		int defencePointInstancesAwarded = 0;
		List<Player> pillagingPlayers = new ArrayList<>();

		//Evaluate the siege zone only if the siege is 'in progress'.
		if(siegeZone.getSiege().getStatus() != SiegeStatus.IN_PROGRESS) 
			return;
		
		NationsUniverse universe = NationsUniverse.getInstance();
		Resident resident;

		//Cycle all online players
		for (Player player : BukkitTools.getOnlinePlayers()) {

			try {
				resident = universe.getDataSource().getResident(player.getName());

				if (resident.hasTown()) {
					Town residentTown= resident.getTown();

					//Residents of occupied towns cannot affect siege points
					if(resident.getTown().isOccupied())
						continue;

					if (defencePointInstancesAwarded <= Settings.getWarSiegeMaxPlayersPerSideForTimedPoints()
						&& residentTown == siegeZone.getDefendingTown()
						&& universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_TOWN_SIEGE_POINTS.getNode())) {

						//Resident of defending town
						defencePointsAwarded = evaluateSiegeZoneOccupant(
												player,
												siegeZone,
												siegeZone.getDefenderPlayerScoreTimeMap(),
												-Settings.getWarSiegePointsForDefenderOccupation());

						if(defencePointsAwarded) {
							defencePointInstancesAwarded++;
						}

					} else if (residentTown.hasNation()
						&& universe.getPermissionSource().testPermission(player, PermissionNodes.TOWNY_NATION_SIEGE_POINTS.getNode())) {

						if (defencePointInstancesAwarded <= Settings.getWarSiegeMaxPlayersPerSideForTimedPoints()
							    && siegeZone.getDefendingTown().hasNation()
								&& siegeZone.getDefendingTown().getNation() == residentTown.getNation()) {

							//Nation member of defending town
							defencePointsAwarded =
											evaluateSiegeZoneOccupant(
													player,
													siegeZone,
													siegeZone.getDefenderPlayerScoreTimeMap(),
													-Settings.getWarSiegePointsForDefenderOccupation());

							if(defencePointsAwarded) {
								defencePointInstancesAwarded++;
							}

						} else if (attackPointInstancesAwarded <= Settings.getWarSiegeMaxPlayersPerSideForTimedPoints()
							&& siegeZone.getAttackingNation() 
							== residentTown.getNation()) {

							//Nation member of attacking nation
							attackPointsAwarded = evaluateSiegeZoneOccupant(
													player,
													siegeZone,
													siegeZone.getAttackerPlayerScoreTimeMap(),
													Settings.getWarSiegePointsForAttackerOccupation());

							if(attackPointsAwarded) {
								attackPointInstancesAwarded++;
								pillagingPlayers.add(player);
							}

						} else if (defencePointInstancesAwarded <= Settings.getWarSiegeMaxPlayersPerSideForTimedPoints()
							&& siegeZone.getDefendingTown().hasNation()
							&& siegeZone.getDefendingTown().getNation().hasMutualAlly(residentTown.getNation())) {

							//Nation member of ally of defending nation
							defencePointsAwarded =
											evaluateSiegeZoneOccupant(
													player,
													siegeZone,
													siegeZone.getDefenderPlayerScoreTimeMap(),
													-Settings.getWarSiegePointsForDefenderOccupation());

							if(defencePointsAwarded) {
								defencePointInstancesAwarded++;
							}

						} else if (attackPointInstancesAwarded <= Settings.getWarSiegeMaxPlayersPerSideForTimedPoints()
							&& siegeZone.getAttackingNation().hasMutualAlly(residentTown.getNation())) {

							//Nation member of ally of attacking nation
							attackPointsAwarded =
											evaluateSiegeZoneOccupant(
													player,
													siegeZone,
													siegeZone.getAttackerPlayerScoreTimeMap(),
													Settings.getWarSiegePointsForAttackerOccupation());

							if(attackPointsAwarded) {
								attackPointInstancesAwarded++;
								pillagingPlayers.add(player);
							}
						}
					}
				}
			} catch (NotRegisteredException e) {
			}
		}

		//Pillage
		double maximumPillageAmount = Settings.getWarSiegeMaximumPillageAmountPerPlot() * siegeZone.getDefendingTown().getTownBlocks().size();
		if(Settings.getWarSiegePillagingEnabled()
			&& Settings.isUsingEconomy()
			&& !siegeZone.getDefendingTown().isNeutral()
			&& siegeZone.getDefendingTown().getSiege().getTotalPillageAmount() < maximumPillageAmount)
		{
			SiegeWarMoneyUtil.pillageTown(pillagingPlayers, siegeZone.getAttackingNation(), siegeZone.getDefendingTown());
		}

		//Save siege zone to db if it was changed
		if(attackPointInstancesAwarded > 0 || defencePointInstancesAwarded > 0) {
			universe.getDataSource().saveSiegeZone(siegeZone);
		}
	}

	/**
	 * Evaluate just 1 siege
	 * 
	 * @param siege
	 */
	private static void evaluateSiege(Siege siege) {
		NationsUniverse universe = NationsUniverse.getInstance();
		
		//Process active siege
		if (siege.getStatus() == SiegeStatus.IN_PROGRESS) {

			//If scheduled end time has arrived, choose winner
			if (System.currentTimeMillis() > siege.getScheduledEndTime()) {
				NationsObject siegeWinner = SiegeWarPointsUtil.calculateSiegeWinner(siege);
				if (siegeWinner instanceof Town) {
					DefenderWin.defenderWin(siege, (Town) siegeWinner);
				} else {
					AttackerWin.attackerWin(siege, (Nation) siegeWinner);
				}

				//Save changes to db
				NationsUniverse townyUniverse = NationsUniverse.getInstance();
				townyUniverse.getDataSource().saveTown(siege.getDefendingTown());
			}

		} else {

			//Siege is finished.
			//Wait for siege immunity timer to end then delete siege
			if (System.currentTimeMillis() > siege.getDefendingTown().getSiegeImmunityEndTime()) {
				universe.getDataSource().removeSiege(siege);
			}
		}
	}
	
	/**
	 * Evaluate 1 siege zone player occupant.
	 * Adjust siege points depending on how long the player has remained-in/occupied the scoring zone
	 * The occupation time requirement is configurable
	 * The siege points adjustment is configurable
	 * 
	 * @param player the player in the siegezone
	 * @param siegeZone the siegezone
	 * @param playerScoreTimeMap the map recording player arrival times 
	 * @param siegePointsForZoneOccupation the int siege points adjustment which will occur if occupation is verified
	 * @return true if the siege zone has been updated
	 */
	private static boolean evaluateSiegeZoneOccupant(Player player,
													 SiegeZone siegeZone,
													 Map<Player, Long> playerScoreTimeMap,
													 int siegePointsForZoneOccupation) {
		
		//Is the player already registered as being in the siege zone ?
		if (playerScoreTimeMap.containsKey(player)) {
			
			//Player must still be in zone
			if (!SiegeWarPointsUtil.isPlayerInSiegePointZone(player, siegeZone)) {
				playerScoreTimeMap.remove(player);
				siegeZone.getPlayerAfkTimeMap().remove(player);
				return false;
			}

			//Player must be alive
			if(player.isDead()) {
				playerScoreTimeMap.remove(player);
				siegeZone.getPlayerAfkTimeMap().remove(player);
				return false;
			}

			//Player must not be flying or invisible
			if(player.isFlying() || player.getPotionEffect(PotionEffectType.INVISIBILITY) != null) {
				playerScoreTimeMap.remove(player);
				siegeZone.getPlayerAfkTimeMap().remove(player);
				return false;
			}

			//Player must still be in the open
			if(SiegeWarBlockUtil.doesPlayerHaveANonAirBlockAboveThem(player)) {
				playerScoreTimeMap.remove(player);
				siegeZone.getPlayerAfkTimeMap().remove(player);
				return false;
			}

			//Player must not have been in zone too long (anti-afk feature)
			if (System.currentTimeMillis() > siegeZone.getPlayerAfkTimeMap().get(player)) {
				Messages.sendErrorMsg(player, Settings.getLangString("msg_err_siege_war_cannot_occupy_zone_for_too_long"));
				playerScoreTimeMap.remove(player);
				return false;
			}

			//Points awarded
			siegePointsForZoneOccupation = SiegeWarPointsUtil.adjustSiegePointGainForCurrentSiegePointBalance(siegePointsForZoneOccupation, siegeZone);
			siegeZone.adjustSiegePoints(siegePointsForZoneOccupation);

			return true;

		} else {

			//Player must be in zone
			if (!SiegeWarPointsUtil.isPlayerInSiegePointZone(player, siegeZone)) {
				return false;
			}

			//Player must be alive
			if(player.isDead()) {
				return false;
			}

			//Player must not be flying or invisible
			if(player.isFlying() || player.getPotionEffect(PotionEffectType.INVISIBILITY) != null) {
				return false;
			}

			//Player must be in the open
			if(SiegeWarBlockUtil.doesPlayerHaveANonAirBlockAboveThem(player)) {
				return false;
			}

			playerScoreTimeMap.put(player, 0L);

			siegeZone.getPlayerAfkTimeMap().put(player,
					System.currentTimeMillis()
							+ (long)(Settings.getWarSiegeZoneMaximumScoringDurationMinutes() * ONE_MINUTE_IN_MILLIS));

			return false; //Player added to zone
		}
	}
	
	/**
	 * Get all the sieges in the universe
	 * 
	 * @return list of all the sieges in the universe
	 */
	private static List<Siege> getAllSieges() {
		List<Siege> result = new ArrayList<>();
		for(Town town: NationsUniverse.getInstance().getDataSource().getTowns()) {
			if(town.hasSiege()) {
				result.add(town.getSiege());
			}
		}
		return result;
	}

}