package com.steffbeard.totalwar.nations.utils;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.steffbeard.totalwar.nations.Messages;
import com.steffbeard.totalwar.nations.objects.Alliance;

public class NationUtils extends Nation {

	private Alliance alliance;
	
	public Alliance getAlliance() throws NotRegisteredException {
		
		if (hasAlliance())
			return alliance;
		else
			throw new NotRegisteredException(Messages.msg_err_nation_doesnt_belong_to_any_alliance);
	}
}
