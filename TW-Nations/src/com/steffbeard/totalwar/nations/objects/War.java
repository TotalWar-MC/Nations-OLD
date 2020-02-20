package com.steffbeard.totalwar.nations.objects;

import com.palmergames.bukkit.towny.object.Nation;
//import com.steffbeard.totalwar.nations.Config;

public class War {
	
	protected Nation nation1, nation2;
	protected Alliance alliance1, alliance2;
	//private Config config;
	//private int dpPerPlayer = config.dpPerPlayer;
	//private int dpPerPlot = config.dpPerPlot;
	
	public War(Nation nat, Nation onat) {
		nation1 = nat;
		nation2 = onat;
		recalculatePoints(nat);
		recalculatePoints(onat);
	}

	private void recalculatePoints(Nation nat) {
		// TODO 
		
	}

	public void setDefender(Nation nation1, Alliance alliance1) {
		//if (callAlly()) {
		//	this.alliance1 = alliance1;
		//} else
		
		this.nation1 = nation1;
	}

	public void setBelligerent(Nation nation2) {
		//if (callAlly()) {

		//} else
		
		this.nation2 = nation2;
	}
}
