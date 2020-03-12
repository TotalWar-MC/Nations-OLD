package com.steffbeard.totalwar.nations.utils;

import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
import com.steffbeard.totalwar.nations.Messages;
import com.steffbeard.totalwar.nations.objects.Alliance;

public class NationUtils extends Nation {

	public NationUtils(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	private Alliance alliance;
	
	public Alliance getAlliance() throws NotRegisteredException {
		
		if (hasAlliance())
			return alliance;
		else
			throw new NotRegisteredException(Messages.msg_err_nation_doesnt_belong_to_any_alliance);
	}
	
	public void setAlliance(Alliance alliance) throws AlreadyRegisteredException {

		if (alliance == null) {
			this.alliance = null;
			return;
		}
		if (this.alliance == alliance)
			return;
		if (hasAlliance())
			throw new AlreadyRegisteredException();
		this.alliance = alliance;
	}
	
	public boolean hasAlliance() {

		return alliance != null;
	}

	public void remove(NationUtils nation) {
		// TODO Auto-generated method stub
		
	}
}
