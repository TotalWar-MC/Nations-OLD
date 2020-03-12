package com.steffbeard.totalwar.nations.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;


import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.palmergames.bukkit.util.BukkitTools;

import com.steffbeard.totalwar.nations.Config;
import com.steffbeard.totalwar.nations.Main;
import com.steffbeard.totalwar.nations.Messages;
import com.steffbeard.totalwar.nations.event.AllianceAddNationEvent;
import com.steffbeard.totalwar.nations.event.AllianceRemoveNationEvent;
import com.steffbeard.totalwar.nations.event.AllianceTagChangeEvent;
import com.steffbeard.totalwar.nations.exceptions.EmptyAllianceException;
import com.steffbeard.totalwar.nations.invites.Invite;
import com.steffbeard.totalwar.nations.invites.InviteHandler;
import com.steffbeard.totalwar.nations.utils.NationUtils;

public class Alliance extends TownyObject {
	
	private Main plugin;
	
	private List<Nation> nations = new ArrayList<>();
	private List<Alliance> enemies = new ArrayList<>();
	private Config config;
	private Nation capital;
	private String nationBoard = "/alliance set board [msg]";
	private String tag = "";
	public UUID uuid;
	private long  registered;
	private boolean isPublic = config.defaultPublic;
	private boolean isOpen = config.defaultOpen;
	private transient List<Invite> receivedinvites = new ArrayList<>();
	private transient List<Invite> sentinvites = new ArrayList<>();
	private transient List<Invite> sentallyinvites = new ArrayList<>();
	
	public Alliance(String name) {
		super();
	}

	public void setTag(String text) throws TownyException {

		if (text.length() > 4) {
			throw new TownyException(Messages.tag_too_long);
		}
		this.tag = text.toUpperCase().trim();
		Bukkit.getPluginManager().callEvent(new AllianceTagChangeEvent(this.tag));
	}

	public String getTag() {

		return tag;
	}

	public boolean hasTag() {

		return !tag.isEmpty();
	}

	public void addEnemy(Alliance alliance) throws AlreadyRegisteredException {

		if (hasEnemy(alliance))
			throw new AlreadyRegisteredException();
		else {
			getEnemies().add(alliance);
		}

	}

	public boolean removeEnemy(Alliance alliance) throws NotRegisteredException {

		if (!hasEnemy(alliance))
			throw new NotRegisteredException();
		else
			return getEnemies().remove(alliance);
	}

	public boolean removeAllEnemies() {

		for (Alliance enemy : new ArrayList<>(getEnemies()))
			try {
				removeEnemy(enemy);
				enemy.removeEnemy(this);
			} catch (NotRegisteredException ignored) {}
		return getEnemies().size() == 0;
	}

	public boolean hasEnemy(Alliance alliance) {

		return getEnemies().contains(alliance);
	}

	public List<Nation> getNations() {

		return nations;
	}

//	public boolean isLeader(Resident resident) {

	//	return hasCapital() && getCapital().isMayor(resident);
//	}

	public boolean hasKing(Resident resident) {

		return getKings().contains(resident);
	}

	public boolean hasNation(String name) {

		for (Nation nation : nations)
			if (nation.getName().equalsIgnoreCase(name))
				return true;
		return false;
	}

	public boolean hasNation(Nation nation) {

		return nations.contains(nation);
	}

	public void addNation(NationUtils nation) throws AlreadyRegisteredException {

		if (hasNation(nation))
			throw new AlreadyRegisteredException();
		else if (nation.hasAlliance())
			throw new AlreadyRegisteredException();
		else {
			nations.add(nation);
			nation.setAlliance(this);
			
			BukkitTools.getPluginManager().callEvent(new AllianceAddNationEvent(nation, this));
		}
	}

	public boolean setAllegiance(String type, Nation nation, Alliance alliance) {

		try {
			if (type.equalsIgnoreCase("truce") || type.equalsIgnoreCase("neutral")) {
				removeEnemy(alliance);
				if (!hasEnemy(alliance))
					return true;
			} else if (type.equalsIgnoreCase("enemy")) {
				addEnemy(alliance);
				if (hasEnemy(alliance))
					return true;
			}
		} catch (AlreadyRegisteredException | NotRegisteredException x) {
			return false;
		}
		
		return false;
	}

	public List<Resident> getKings() {

		List<Resident> kings = new ArrayList<>();
		
		for (Nation nation: nations)
		for (Resident king: nation.getResidents()) {
			if (king.hasNationRank("king"))
				kings.add(king);
		}
		return kings;
	}

	public void setEnemies(List<Alliance> enemies) {

		this.enemies = enemies;
	}
	public List<Alliance> getEnemies() {

		return enemies;
	}

	public int getNumNations() {

		return nations.size();
	}

	public void removeAlliance(Nation nation) throws EmptyAllianceException, NotRegisteredException {

		if (!hasNation(nation)) {
			throw new NotRegisteredException();
		} else {

			remove((NationUtils) nation);

			if (getNumNations() == 0) {
				throw new EmptyAllianceException(this);
			}
		}
	}

	private void remove(NationUtils nation) {

		//removeAssistantsIn(town);
		try {
			nation.setAlliance(null);
		} catch (AlreadyRegisteredException ignored) {
		}
		
		nation.remove(nation);
		
		BukkitTools.getPluginManager().callEvent(new AllianceRemoveNationEvent(nation, this));
	}

	private void removeAllNations() {

		for (Nation nation : new ArrayList<>(nations))
			remove(nation);
	}

	public void clear() {

		//TODO: Check cleanup
		removeAllEnemies();
		removeAllNations();
	}

	public boolean hasResident(Resident resident) {
		
		for (Nation nation : getNations())
			if (((Nation) nation.getTowns()).hasResident(resident));
				return true;
	}

	public List<Resident> getResidents() {

		List<Resident> out = new ArrayList<>();
		for (Nation nation : getNations())
			out.addAll(((Nation) nation.getTowns()).getResidents());
		return out;
	}

	@Override
	public List<String> getTreeString(int depth) {

		List<String> out = new ArrayList<>();
		out.add(getTreeDepth(depth) + "Alliance (" + getName() + ")");
		
		List<Resident> kings = getKings();
		
		if (kings.size() > 0)
			out.add(getTreeDepth(depth + 1) + "Leaders (" + kings.size() + "): " + Arrays.toString(kings.toArray(new Resident[0])));
		if (getEnemies().size() > 0)
			out.add(getTreeDepth(depth + 1) + "Enemies (" + getEnemies().size() + "): " + Arrays.toString(getEnemies().toArray(new Nation[0])));
		out.add(getTreeDepth(depth + 1) + "Nations (" + getNations().size() + "):");
		for (Nation nation : getNations())
			out.addAll(nation.getTreeString(depth + 2));
		return out;
	}

	public List<Resident> getOutlaws() {

		List<Resident> out = new ArrayList<>();
		for (Nation nation : getNations())
			out.addAll(((Nation) nation.getTowns()).getOutlaws());
		return out;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public boolean hasValidUUID() {
		return uuid != null;
	}

	public long getRegistered() {
		return registered;
	}

	public void setRegistered(long registered) {
		this.registered = registered;
	}

	public List<Invite> getReceivedInvites() {
		return receivedinvites;
	}

	public void newReceivedInvite(Invite invite) throws TooManyInvitesException {
		if (receivedinvites.size() <= (InviteHandler.getReceivedInvitesMaxAmount(this) -1)) {
			receivedinvites.add(invite);
		} else {
			throw new TooManyInvitesException();
		}
	}

	public void deleteReceivedInvite(Invite invite) {
		receivedinvites.remove(invite);
	}

	public List<Invite> getSentInvites() {
		return sentinvites;
	}

	public void newSentInvite(Invite invite) throws TooManyInvitesException {
		if (sentinvites.size() <= (InviteHandler.getSentInvitesMaxAmount(this) -1)) {
			sentinvites.add(invite);
		} else {
			throw new TooManyInvitesException();
		}
	}

	public void deleteSentInvite(Invite invite) {
		sentinvites.remove(invite);
	}
	
	public void newSentAllyInvite(Invite invite) throws TooManyInvitesException {
		if (sentallyinvites.size() <= InviteHandler.getSentAllyRequestsMaxAmount(this) -1) {
			sentallyinvites.add(invite);
		} else {
			throw new TooManyInvitesException();
		}
	}
	
	public void deleteSentAllyInvite(Invite invite) {
		sentallyinvites.remove(invite);
	}
	
	public List<Invite> getSentAllyInvites() {
		return sentallyinvites;
	}
	
	public void setNationBoard(String nationBoard) {

		this.nationBoard = nationBoard;
	}

	public String getNationBoard() {
		return nationBoard;
	}

    public void setPublic(boolean isPublic) {

        this.isPublic = isPublic;
    }

    public boolean isPublic() {

        return isPublic;
    }
    
    public void setOpen(boolean isOpen) {
    	
    	this.isOpen = isOpen;
    }
    
    public boolean isOpen() {
    	
    	return isOpen;
    }

    /*
     * TODO:
     * Have to add MySQL support
     */
    
	@Override
	public String getFormattedName() {
		return TownySettings.getNationPrefix(this) + this.getName().replaceAll("_", " ")
			+ TownySettings.getNationPostfix(this);
	}

	public void addMetaData(CustomDataField md) {
		super.addMetaData(md);

		plugin.getInstance().getDataSource().saveNation(this);
	}

	public void removeMetaData(CustomDataField md) {
		super.removeMetaData(md);

		plugin.getInstance().getDataSource().saveNation(this);
	}
	
}
