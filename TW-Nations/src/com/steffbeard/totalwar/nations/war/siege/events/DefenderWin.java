package com.steffbeard.totalwar.nations.war.siege.events;

import com.steffbeard.totalwar.nations.config.Messages;
import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.objects.town.Town;
import com.steffbeard.totalwar.nations.war.siege.SiegeStatus;
import com.steffbeard.totalwar.nations.war.siege.location.Siege;

/**
 * This class is responsible for processing siege defender wins
 *
 * @author Goosius
 */
public class DefenderWin
{
	/**
	 * This method triggers siege values to be updated for a defender win
	 *
	 * @param siege the siege
	 * @param winnerTown the winning town
	 */
    public static void defenderWin(Siege siege, Town winnerTown) {
        SiegeWarSiegeCompletionUtil.updateSiegeValuesToComplete(siege, SiegeStatus.DEFENDER_WIN, null);

		Messages.sendGlobalMessage(String.format(
			Settings.getLangString("msg_siege_war_defender_win"),
			winnerTown.getFormattedName()));

		SiegeWarMoneyUtil.giveWarChestsToWinnerTown(siege, winnerTown);
    }

}
