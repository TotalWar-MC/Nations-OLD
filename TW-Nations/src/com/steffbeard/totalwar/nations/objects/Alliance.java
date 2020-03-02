package com.steffbeard.totalwar.nations.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.invites.exceptions.TooManyInvitesException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyObject;
import com.steffbeard.totalwar.nations.Config;
import com.steffbeard.totalwar.nations.invites.Invite;

public class Alliance extends TownyObject {
	
	private List<Town> towns = new ArrayList<>();
	private List<Alliance> allies = new ArrayList<>();
	private List<Alliance> enemies = new ArrayList<>();
	private Config config;
	private Nation capital;
	private double taxes, spawnCost;
	private boolean neutral = false;
	private String nationBoard = "/alliance set board [msg]";
	private String tag = "";
	public UUID uuid;
	private long registered;
	private boolean isPublic = config.defaultPublic;
	private boolean isOpen = config.defaultOpen;
	private transient List<Invite> receivedinvites = new ArrayList<>();
	private transient List<Invite> sentinvites = new ArrayList<>();
	private transient List<Invite> sentallyinvites = new ArrayList<>();
	
	public Nation(String name) {
		super(name);
	}

	public void setTag(String text) throws TownyException {

		if (text.length() > 4) {
			throw new TownyException(TownySettings.getLangString("msg_err_tag_too_long"));
		}
		this.tag = text.toUpperCase().trim();
		Bukkit.getPluginManager().callEvent(new NationTagChangeEvent(this.tag));
	}

	public String getTag() {

		return tag;
	}

	public boolean hasTag() {

		return !tag.isEmpty();
	}

	public void addAlly(Nation nation) throws AlreadyRegisteredException {

		if (hasAlly(nation))
			throw new AlreadyRegisteredException();
		else {
			try {
				removeEnemy(nation);
			} catch (NotRegisteredException ignored) {}
			getAllies().add(nation);
		}
	}

	public boolean removeAlly(Nation nation, Alliance alliance) throws NotRegisteredException {

		if (!hasAlly(nation) && !hasAlly(alliance))
			throw new NotRegisteredException();
		else
			return getAllies().remove(nation);
			return getAllies().remove(alliance);
	}

	public boolean removeAllAllies() {

		for (Alliance ally : new ArrayList<>(getAllies()))
			try {
				removeAlly(ally);
				ally.removeAlly(this);
			} catch (NotRegisteredException ignored) {}
		return getAllies().size() == 0;
	}

	public boolean hasAlly(Nation nation) {

		return getAllies().contains(nation);
	}

	public boolean IsAlliedWith(Nation nation) {

		return getAllies().contains(nation);
	}

	public void addEnemy(Nation nation) throws AlreadyRegisteredException {

		if (hasEnemy(nation))
			throw new AlreadyRegisteredException();
		else {
			try {
				removeAlly(nation);
			} catch (NotRegisteredException ignored) {}
			getEnemies().add(nation);
		}

	}

	public boolean removeEnemy(Nation nation) throws NotRegisteredException {

		if (!hasEnemy(nation))
			throw new NotRegisteredException();
		else
			return getEnemies().remove(nation);
	}

	public boolean removeAllEnemies() {

		for (Nation enemy : new ArrayList<>(getEnemies()))
			try {
				removeEnemy(enemy);
				enemy.removeEnemy(this);
			} catch (NotRegisteredException ignored) {}
		return getAllies().size() == 0;
	}

	public boolean hasEnemy(Nation nation) {

		return getEnemies().contains(nation);
	}

	public List<Town> getTowns() {

		return towns;
	}

	public boolean isKing(Resident resident) {

		return hasCapital() && getCapital().isMayor(resident);
	}

	public boolean hasCapital() {

		return getCapital() != null;
	}

	public boolean hasAssistant(Resident resident) {

		return getAssistants().contains(resident);
	}

	public boolean isCapital(Town town) {

		return town == getCapital();
	}

	public boolean hasTown(String name) {

		for (Town town : towns)
			if (town.getName().equalsIgnoreCase(name))
				return true;
		return false;
	}

	public boolean hasTown(Town town) {

		return towns.contains(town);
	}

	public void addTown(Town town) throws AlreadyRegisteredException {

		if (hasTown(town))
			throw new AlreadyRegisteredException();
		else if (town.hasNation())
			throw new AlreadyRegisteredException();
		else {
			towns.add(town);
			town.setNation(this);
			
			BukkitTools.getPluginManager().callEvent(new NationAddTownEvent(town, this));
		}
	}

	public void setCapital(Town capital) {

		this.capital = capital;
		try {
			recheckTownDistance();
			TownyPerms.assignPermissions(capital.getMayor(), null);
		} catch (Exception e) {
			// Dummy catch to prevent errors on startup when setting nation.
		}
	}

	public Town getCapital() {

		return capital;
	}

	public Location getNationSpawn() throws TownyException {
		if(nationSpawn == null){
			throw new TownyException(TownySettings.getLangString("msg_err_nation_has_not_set_a_spawn_location"));
		}

		return nationSpawn;
	}

	public boolean hasNationSpawn(){
		return (nationSpawn != null);
	}

	public void setNationSpawn(Location spawn) throws TownyException {
		Coord spawnBlock = Coord.parseCoord(spawn);
		TownBlock townBlock;
		TownyWorld world = TownyUniverse.getInstance().getDataSource().getWorld(spawn.getWorld().getName()); 
		if (world.hasTownBlock(spawnBlock))
			townBlock = world.getTownBlock(spawnBlock);
		else 
			throw new TownyException(String.format(TownySettings.getLangString("msg_cache_block_error_wild"), "set spawn"));

		if(TownySettings.getBoolean(ConfigNodes.GNATION_SETTINGS_CAPITAL_SPAWN)){
			if(this.capital == null){
				throw new TownyException(TownySettings.getLangString("msg_err_spawn_not_within_capital"));
			}
			if(!townBlock.hasTown()){
				throw new TownyException(TownySettings.getLangString("msg_err_spawn_not_within_capital"));
			}
			if(townBlock.getTown() != this.getCapital()){
				throw new TownyException(TownySettings.getLangString("msg_err_spawn_not_within_capital"));
			}
		} else {
			if(!townBlock.hasTown()){
				throw new TownyException(TownySettings.getLangString("msg_err_spawn_not_within_nationtowns"));
			}

			if(!towns.contains(townBlock.getTown())){
				throw new TownyException(TownySettings.getLangString("msg_err_spawn_not_within_nationtowns"));
			}
		}

		this.nationSpawn = spawn;
	}

	/**
	 * Only to be called from the Loading methods.
	 *
	 * @param nationSpawn - Location to set as Nation Spawn
	 */
	public void forceSetNationSpawn(Location nationSpawn){
		this.nationSpawn = nationSpawn;
	}

	//TODO: Remove
	public boolean setAllegiance(String type, Nation nation, Alliance alliance) {

		try {
			if (type.equalsIgnoreCase("ally")) {
				removeEnemy(nation);
				addAlly(nation);
				if (!hasEnemy(nation) && hasAlly(nation))
					return true;
			} else if (type.equalsIgnoreCase("peaceful") || type.equalsIgnoreCase("neutral")) {
				removeEnemy(nation);
				removeAlly(nation);
				if (!hasEnemy(nation) && !hasAlly(nation))
					return true;
			} else if (type.equalsIgnoreCase("enemy")) {
				removeAlly(nation);
				addEnemy(nation);
				if (hasEnemy(nation) && !hasAlly(nation))
					return true;
			}
			if (type.equalsIgnoreCase("ally")) {
				removeEnemy(alliance);
				addAlly(alliance));
				if (!hasEnemy(nation) && hasAlly(alliance))
					return true;
			} else if (type.equalsIgnoreCase("peaceful") || type.equalsIgnoreCase("neutral")) {
				removeEnemy(alliance);
				removeAlly(alliance);
				if (!hasEnemy(alliance) && !hasAlly(alliance))
					return true;
			} else if (type.equalsIgnoreCase("enemy")) {
				removeAlly(alliance);
				addEnemy(alliance);
				if (hasEnemy(alliance) && !hasAlly(alliance))
					return true;
			}
		} catch (AlreadyRegisteredException | NotRegisteredException x) {
			return false;
		}
		
		return false;
	}

	public List<Resident> getAssistants() {

		List<Resident> assistants = new ArrayList<>();
		
		for (Town town: towns)
		for (Resident assistant: town.getResidents()) {
			if (assistant.hasNationRank("assistant"))
				assistants.add(assistant);
		}
		return assistants;
	}

	public void setEnemies(List<Alliance> enemies) {

		this.enemies = enemies;
	}

	public List<Alliance> getEnemies() {

		return enemies;
	}

	public void setAllies(List<Alliance> allies) {

		this.allies = allies;
	}

	public List<Alliance> getAllies() {

		return allies;
	}

	public int getNumTowns() {

		return towns.size();
	}

	public int getNumResidents() {

		int numResidents = 0;
		for (Town town : getTowns())
			numResidents += town.getNumResidents();
		return numResidents;
	}

	public void removeNation(Nation nation) throws EmptyNationException, NotRegisteredException {

		if (!hasNation(nation))
			throw new NotRegisteredException();
		else {

			boolean isCapital = nation.isCapital();
			remove(nation));

			if (getNumTowns() == 0) {
				throw new EmptyAllianceException(this);
			} else if (isCapital) {
				int numResidents = 0;
				Nation tempCapital = null;
				for (Nation newCapital : getNations())
					if (newCapital.getNumResidents() > numResidents) {
						tempCapital = newCapital;
						numResidents = newCapital.getNumResidents();
					}

				if (tempCapital != null) {
					setCapital(tempCapital);
				}

			}
		}
	}

	private void remove(Nation nation) {

		//removeAssistantsIn(town);
		try {
			nation.setAlliance(null);
		} catch (AlreadyRegisteredException ignored) {
		}
		
		nation.remove(nation);
		
		BukkitTools.getPluginManager().callEvent(new NationRemoveTownEvent(town, this));
	}

	private void removeAllNations() {

		for (Nation nation : new ArrayList<>(nations))
			remove(nation);
	}

	public void clear() {

		//TODO: Check cleanup
		removeAllAllies();
		removeAllEnemies();
		removeAllTowns();
		capital = null;
	}

	/**
	 * Method for rechecking town distances to a new nation capital/moved nation capital homeblock.
	 * @throws TownyException - Generic TownyException
	 */

	public void setKing(Resident king) throws TownyException {

		if (!hasResident(king))
			throw new TownyException(TownySettings.getLangString("msg_err_king_not_in_nation"));
		if (!king.isMayor())
			throw new TownyException(TownySettings.getLangString("msg_err_new_king_notmayor"));
		setCapital(king.getTown());
	}

	public boolean hasResident(Resident resident) {

		for (Town town : getTowns())
			if (town.hasResident(resident))
				return true;
		return false;
	}

	@Override
	public List<Resident> getResidents() {

		List<Resident> out = new ArrayList<>();
		for (Town town : getTowns())
			out.addAll(town.getResidents());
		return out;
	}

	@Override
	public List<String> getTreeString(int depth) {

		List<String> out = new ArrayList<>();
		out.add(getTreeDepth(depth) + "Nation (" + getName() + ")");
		out.add(getTreeDepth(depth + 1) + "Capital: " + getCapital().getName());
		
		List<Resident> assistants = getAssistants();
		
		if (assistants.size() > 0)
			out.add(getTreeDepth(depth + 1) + "Assistants (" + assistants.size() + "): " + Arrays.toString(assistants.toArray(new Resident[0])));
		if (getAllies().size() > 0)
			out.add(getTreeDepth(depth + 1) + "Allies (" + getAllies().size() + "): " + Arrays.toString(getAllies().toArray(new Nation[0])));
		if (getEnemies().size() > 0)
			out.add(getTreeDepth(depth + 1) + "Enemies (" + getEnemies().size() + "): " + Arrays.toString(getEnemies().toArray(new Nation[0])));
		out.add(getTreeDepth(depth + 1) + "Towns (" + getTowns().size() + "):");
		for (Town town : getTowns())
			out.addAll(town.getTreeString(depth + 2));
		return out;
	}

	@Override
	public boolean hasResident(String name) {

		for (Town town : getTowns())
			if (town.hasResident(name))
				return true;
		return false;
	}

	@Override
	public List<Resident> getOutlaws() {

		List<Resident> out = new ArrayList<>();
		for (Town town : getTowns())
			out.addAll(town.getOutlaws());
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

	@Override
	public List<Invite> getReceivedInvites() {
		return receivedinvites;
	}

	@Override
	public void newReceivedInvite(Invite invite) throws TooManyInvitesException {
		if (receivedinvites.size() <= (InviteHandler.getReceivedInvitesMaxAmount(this) -1)) {
			receivedinvites.add(invite);
		} else {
			throw new TooManyInvitesException(String.format(TownySettings.getLangString("msg_err_nation_has_too_many_requests"),this.getName()));
		}
	}

	@Override
	public void deleteReceivedInvite(Invite invite) {
		receivedinvites.remove(invite);
	}

	@Override
	public List<Invite> getSentInvites() {
		return sentinvites;
	}

	@Override
	public void newSentInvite(Invite invite) throws TooManyInvitesException {
		if (sentinvites.size() <= (InviteHandler.getSentInvitesMaxAmount(this) -1)) {
			sentinvites.add(invite);
		} else {
			throw new TooManyInvitesException(TownySettings.getLangString("msg_err_nation_sent_too_many_invites"));
		}
	}

	@Override
	public void deleteSentInvite(Invite invite) {
		sentinvites.remove(invite);
	}
	
	public void newSentAllyInvite(Invite invite) throws TooManyInvitesException {
		if (sentallyinvites.size() <= InviteHandler.getSentAllyRequestsMaxAmount(this) -1) {
			sentallyinvites.add(invite);
		} else {
			throw new TooManyInvitesException(TownySettings.getLangString("msg_err_nation_sent_too_many_requests"));
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
    
	public void setSpawnCost(double spawnCost) {

		this.spawnCost = spawnCost;
	}

	public double getSpawnCost() {

		return spawnCost;
	}
	
	public int getNumTownblocks() {
		int townBlocksClaimed = 0;
		for (Town towns : this.getTowns()) {
			townBlocksClaimed = townBlocksClaimed + towns.getTownBlocks().size();
		}
		return townBlocksClaimed;
	}
	
	public Resident getKing() {
		return capital.getMayor();
	}

	@Override
	public String getFormattedName() {
		return TownySettings.getNationPrefix(this) + this.getName().replaceAll("_", " ")
			+ TownySettings.getNationPostfix(this);
	}

	public void addMetaData(CustomDataField md) {
		super.addMetaData(md);

		TownyUniverse.getInstance().getDataSource().saveNation(this);
	}

	public void removeMetaData(CustomDataField md) {
		super.removeMetaData(md);

		TownyUniverse.getInstance().getDataSource().saveNation(this);
	}
	
}
