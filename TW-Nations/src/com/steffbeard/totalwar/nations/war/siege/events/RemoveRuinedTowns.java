package com.steffbeard.totalwar.nations.war.siege.events;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.steffbeard.totalwar.nations.NationsUniverse;
import com.steffbeard.totalwar.nations.config.Messages;
import com.steffbeard.totalwar.nations.objects.town.Town;

/**
 * This class is responsible for removing ruined towns completely
 *
 * @author Goosius
 */
public class RemoveRuinedTowns {

	/**
	 * This method cycles through all towns
	 * It determines which towns have lain in ruins for long enough, and deletes them.
	 */
    public static void deleteRuinedTowns() {
		NationsUniverse townyUniverse = NationsUniverse.getInstance();
		List<Town> towns = new ArrayList<>(townyUniverse.getDataSource().getTowns());
		ListIterator<Town> townItr = towns.listIterator();
		Town town;

		while (townItr.hasNext()) {
			town = townItr.next();
			/*
			 * Only delete ruined town if it really still
			 * exists.
			 * We are running in an Async thread so MUST verify all objects.
			 */
			if (townyUniverse.getDataSource().hasTown(town.getName())) {

				if(town.isRuined() && System.currentTimeMillis() > town.getRecentlyRuinedEndTime()) {
					try {
						//Ruin found & recently ruined end time reached. Delete town now.
						townyUniverse.getDataSource().removeTown(town, false);
					} catch (Exception e){
						Messages.sendErrorMsg("Problem deleting ruined town " + town.getName());
						e.printStackTrace();
					}
				}
			} 
		}
    }

}
