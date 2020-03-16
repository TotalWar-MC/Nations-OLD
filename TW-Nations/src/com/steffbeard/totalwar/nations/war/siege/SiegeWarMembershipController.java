package com.steffbeard.totalwar.nations.war.siege;

import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.objects.nations.Nation;
import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.objects.town.Town;

/**
 * This class intercepts 'remove' requests, where a resident is removed from a town,
 * or a town is removed from a nation.
 *
 * The class evaluates the requests and determines if any siege updates are needed.
 * 
 * @author Goosius
 */
public class SiegeWarMembershipController {

	/**
	 * Evaluates a town removing a resident, and determines if any siege penalty points apply
	 * 
	 * @param resident The resident who is being removed
	 *  
	 */
	public static void evaluateTownRemoveResident(Resident resident) {
		SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, Settings.getLangString("msg_siege_war_resident_leave_town"));
	}
	
	/**
	 * Evaluates a nation removing a town, and determines if any siege penalty points apply
	 *
	 * @param town The town which is being removed
	 *
	 */
	public static void evaluateNationRemoveTown(Town town) {
		for (Resident resident : town.getResidents()) {
				SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, Settings.getLangString("msg_siege_war_town_leave_nation"));
		}
	}

	/**
	 * Evaluates a nation removing an ally, and determines if any siege penalty points apply
	 *
	 * @param ally The ally being removed
	 * 
	 */
	public static void evaluateNationRemoveAlly(Nation nation, Nation ally) {
		for (Resident resident : nation.getResidents()) {
			SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, Settings.getLangString("msg_siege_war_ally_removed"));
		}
		for (Resident resident : ally.getResidents()) {
			SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, Settings.getLangString("msg_siege_war_ally_removed"));
		}
	}

}
