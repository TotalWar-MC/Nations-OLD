package com.steffbeard.totalwar.nations.war.siege;

import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.permissions.NationsPerms;
import com.steffbeard.totalwar.nations.permissions.PermissionNodes;

/**
 * This class intercepts 'remove rank' requests, where a resident's rank is removed.
 *
 * The class evaluates the requests and determines if any siege updates are needed.
 * 
 * @author Goosius
 */
public class SiegeWarRankController {

	/**
	 * Evaluates a town rank being removed, and determines if a siege point penalty applies
	 * 
	 * @param resident The affected resident
	 * @param rank The rank being removed                   
	 *  
	 */
	public static void evaluateTownRemoveRank(Resident resident, String rank) {
		if(NationsPerms.getTownRank(rank).contains(PermissionNodes.TOWNY_TOWN_SIEGE_POINTS.getNode())) {
			SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, Settings.getLangString("msg_siege_war_resident_town_rank_removed"));
		}
	}
	
	/**
	 * Evaluates a nation rank being removed, and determines if a siege point penalty applies
	 *
	 * @param resident The affected resident
	 * @param rank The rank being removed                   
	 *
	 */
	public static void evaluateNationRemoveRank(Resident resident, String rank) {
		if(NationsPerms.getNationRank(rank).contains(PermissionNodes.TOWNY_NATION_SIEGE_POINTS.getNode())) {
			SiegeWarPointsUtil.evaluateSiegePenaltyPoints(resident, Settings.getLangString("msg_siege_war_resident_nation_rank_removed"));
		}
	}
	
}
