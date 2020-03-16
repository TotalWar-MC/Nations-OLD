package com.steffbeard.totalwar.nations.war.siege.events;

import com.steffbeard.totalwar.nations.config.Messages;
import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.objects.nations.Nation;
import com.steffbeard.totalwar.nations.war.siege.SiegeStatus;
import com.steffbeard.totalwar.nations.war.siege.location.Siege;

/**
 * This class is responsible for processing siege attacker wins
 *
 * @author Goosius
 */
public class AttackerWin {

	/**
	 * This method triggers siege values to be updated for an attacker win
	 * 
	 * @param siege the siege
	 * @param winnerNation the winning nation
	 */
	public static void attackerWin(Siege siege, Nation winnerNation) {
        SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siege, SiegeStatus.ATTACKER_WIN, winnerNation);

		Messages.sendGlobalMessage(String.format(
			Settings.getLangString("msg_siege_war_attacker_win"),
			winnerNation.getFormattedName(),
			siege.getDefendingTown().getFormattedName()
		));

		SiegeWarMoneyUtil.giveWarChestsToWinnerNation(siege, winnerNation);
    }
}
