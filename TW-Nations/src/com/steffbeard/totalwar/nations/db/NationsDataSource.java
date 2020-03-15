package com.steffbeard.totalwar.nations.db;

import com.steffbeard.totalwar.nations.Main;
import com.steffbeard.totalwar.nations.Messages;
import com.steffbeard.totalwar.nations.NationsUniverse;
import com.steffbeard.totalwar.nations.exceptions.AlreadyRegisteredException;
import com.steffbeard.totalwar.nations.exceptions.NotRegisteredException;
import com.steffbeard.totalwar.nations.objects.NationsWorld;
import com.steffbeard.totalwar.nations.objects.PlotGroup;
import com.steffbeard.totalwar.nations.objects.Resident;

import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//import java.util.Hashtable;
//import com.palmergames.bukkit.towny.TownySettings;

/*
 * --- : Loading process : ---
 *
 * Load all the names/keys for each world, nation, town, and resident.
 * Load each world, which loads it's town blocks.
 * Load nations, towns, and residents.
 */

/*
 * Loading Towns:
 * Make sure to load TownBlocks, then HomeBlock, then Spawn.
 */

public abstract class NationsDataSource {
	final Lock lock = new ReentrantLock();
	protected final Main plugin;
	protected final NationsUniverse universe;

	NationsDataSource(Main plugin, NationsUniverse universe) {
		this.plugin = plugin;
		this.universe = universe;
	}

	public abstract boolean backup() throws IOException;

	public abstract void cleanupBackups();

	public abstract void deleteUnusedResidents();

	public boolean loadAll() {

		return loadWorldList() && loadNationList() && loadTownList() && loadPlotGroupList() && loadSiegeZoneList() && loadResidentList() && loadTownBlockList() && loadWorlds() && loadNations() && loadTowns() && loadSiegeZones() && loadResidents() && loadTownBlocks() && loadPlotGroups() && loadRegenList() && loadSnapshotList();
	}

	public boolean saveAll() {
		return saveWorldList() && saveNationList() && saveTownList() && savePlotGroupList() && saveSiegeZoneList() && saveResidentList() && saveTownBlockList() && saveWorlds() && saveNations() && saveTowns() && saveResidents() && savePlotGroups() && saveSiegeZones() && saveAllTownBlocks() && saveRegenList() && saveSnapshotList();
	}

	public boolean saveAllWorlds() {

		return saveWorldList() && saveWorlds();
	}

	public boolean saveQueues() {

		return saveRegenList() && saveSnapshotList();
	}

	abstract public void cancelTask();

	abstract public boolean loadTownBlockList();

	abstract public boolean loadResidentList();

	abstract public boolean loadTownList();

	abstract public boolean loadNationList();
	
	abstract public boolean loadAllianceList();

	abstract public boolean loadSiegeZoneList();

	abstract public boolean loadWorldList();

	abstract public boolean loadRegenList();

	abstract public boolean loadSnapshotList();

	abstract public boolean loadTownBlocks();

	abstract public boolean loadResident(Resident resident);

	abstract public boolean loadTown(Town town);

	abstract public boolean loadNation(Nation nation);
	
	abstract public boolean loadAlliance(Alliance alliance);

	abstract public boolean loadSiegeZone(SiegeZone siegeZone);

	abstract public boolean loadWorld(NationsWorld world);

	abstract public boolean loadPlotGroupList();

	abstract public boolean loadPlotGroups();

	abstract public boolean saveTownBlockList();

	abstract public boolean saveResidentList();

	abstract public boolean saveTownList();

	abstract public boolean savePlotGroupList();

	abstract public boolean saveNationList();
	
	abstract public boolean saveAllianceList();

	abstract public boolean saveSiegeZoneList();

	abstract public boolean saveWorldList();

	abstract public boolean saveRegenList();

	abstract public boolean saveSnapshotList();

	abstract public boolean saveResident(Resident resident);

	abstract public boolean saveTown(Town town);
	
	abstract public boolean savePlotGroup(PlotGroup group);

	abstract public boolean saveNation(Nation nation);
	
	abstract public boolean saveAlliance(Alliance alliance);

	abstract public boolean saveSiegeZone(SiegeZone siegeFront);

	abstract public boolean saveWorld(NationsWorld world);

	abstract public boolean saveAllTownBlocks();

	abstract public boolean saveTownBlock(TownBlock townBlock);

	abstract public boolean savePlotData(PlotBlockData plotChunk);

	abstract public PlotBlockData loadPlotData(String worldName, int x, int z);

	abstract public PlotBlockData loadPlotData(TownBlock townBlock);

	abstract public void deletePlotData(PlotBlockData plotChunk);

	abstract public void deleteResident(Resident resident);

	abstract public void deleteTown(Town town);

	abstract public void deleteNation(Nation nation);
	
	abstract public void deleteAlliance(Alliance alliance);

	abstract public void deleteSiegeZone(SiegeZone siegeFront);

	abstract public void deleteWorld(NationsWorld world);

	abstract public void deleteTownBlock(TownBlock townBlock);

	abstract public void deleteFile(String file);
	
	abstract public void deletePlotGroup(PlotGroup group);

	public boolean cleanup() {

		return true;

	}

	public boolean loadResidents() {

		Messages.sendDebugMsg("Loading Residents");

		List<Resident> toRemove = new ArrayList<>();

		for (Resident resident : new ArrayList<>(getResidents()))
			if (!loadResident(resident)) {
				System.out.println("[Nations] Loading Error: Could not read resident data '" + resident.getName() + "'.");
				toRemove.add(resident);
				//return false;
			}

		// Remove any resident which failed to load.
		for (Resident resident : toRemove) {
			System.out.println("[Nations] Loading Error: Removing resident data for '" + resident.getName() + "'.");
			removeResidentList(resident);
		}

		return true;
	}

	public boolean loadTowns() {

		Messages.sendDebugMsg("Loading Towns");
		for (Town town : getTowns())
			if (!loadTown(town)) {
				System.out.println("[Nations] Loading Error: Could not read town data '" + town.getName() + "'.");
				return false;
			}
		return true;
	}

	public boolean loadNations() {

		Messages.sendDebugMsg("Loading Nations");
		for (Nation nation : getNations())
			if (!loadNation(nation)) {
				System.out.println("[Nations] Loading Error: Could not read nation data '" + nation.getName() + "'.");
				return false;
			}
		return true;
	}
	
	public boolean loadAlliances() {

		Messages.sendDebugMsg("Loading Alliances");
		for (Alliance alliance : getAlliances())
			if (!loadAlliances(alliance)) {
				System.out.println("[Nations] Loading Error: Could not read alliance data '" + alliance.getName() + "'.");
				return false;
			}
		return true;
	}

	public boolean loadSiegeZones() {
		Messages.sendDebugMsg("Loading Siege Zones");
		for (SiegeZone siegeZone : getSiegeZones())
			if (!loadSiegeZone(siegeZone)) {
				System.out.println("[Nations] Loading Error: Could not read siege zone data '" + siegeZone.getName() + "'.");
				return false;
			}
		return true;
	}


	public boolean loadWorlds() {

		Messages.sendDebugMsg("Loading Worlds");
		for (NationsWorld world : getWorlds())
			if (!loadWorld(world)) {
				System.out.println("[Nations] Loading Error: Could not read world data '" + world.getName() + "'.");
				return false;
			} else {
				// Push all Towns belonging to this world
			}
		return true;
	}

	/*
	 * Save all of category
	 */

	public boolean saveResidents() {

		Messages.sendDebugMsg("Saving Residents");
		for (Resident resident : getResidents())
			saveResident(resident);
		return true;
	}
	
	public boolean savePlotGroups() {
		Messages.sendDebugMsg("Saving PlotGroups");
		for (PlotGroup plotGroup : getAllPlotGroups())
			savePlotGroup(plotGroup);
		return true;
	}

	public boolean saveTowns() {

		Messages.sendDebugMsg("Saving Towns");
		for (Town town : getTowns())
			saveTown(town);
		return true;
	}

	public boolean saveNations() {

		Messages.sendDebugMsg("Saving Nations");
		for (Nation nation : getNations())
			saveNation(nation);
		return true;
	}
	
	public boolean saveAlliances() {

		Messages.sendDebugMsg("Saving Alliances");
		for (Alliance alliance : getAlliances())
			saveAlliance(alliance);
		return true;
	}

	public boolean saveSiegeZones() {
		Messages.sendDebugMsg("Saving Siege Zones");
		for (SiegeZone siegeZone : getSiegeZones())
			saveSiegeZone(siegeZone);
		return true;
	}

	public boolean saveWorlds() {

		Messages.sendDebugMsg("Saving Worlds");
		for (NationsWorld world : getWorlds())
			saveWorld(world);
		return true;
	}

	// Database functions
	abstract public List<Resident> getResidents(Player player, String[] names);

	abstract public List<Resident> getResidents();
	
	abstract public List<PlotGroup> getAllPlotGroups();

	abstract public List<Resident> getResidents(String[] names);

	abstract public Resident getResident(String name) throws NotRegisteredException;

	abstract public void removeResidentList(Resident resident);

	abstract public void removeNation(Nation nation);
	
	abstract public void removeAlliance(Alliance alliance);

	abstract public boolean hasResident(String name);

	abstract public boolean hasTown(String name);

	abstract public boolean hasNation(String name);
	
	abstract public boolean hasAlliance(String name);

	abstract public List<Town> getTowns(String[] names);

	abstract public List<Town> getTowns();

	abstract public Town getTown(String name) throws NotRegisteredException;

	abstract public Town getTown(UUID uuid) throws NotRegisteredException;
	
	abstract public SiegeZone getSiegeZone(String name) throws NotRegisteredException;
		
	abstract public List<Nation> getNations(String[] names);

	abstract public List<Nation> getNations();
	
	abstract public List<Alliance> getAlliances(String[] names);

	abstract public List<Alliance> getAlliances();

	abstract public List<SiegeZone> getSiegeZones();

	abstract public Nation getNation(String name) throws NotRegisteredException;

	abstract public Nation getNation(UUID uiid) throws NotRegisteredException;
	
	abstract public Alliance getAlliance(String name) throws NotRegisteredException;

	abstract public Alliance getAlliance(UUID uiid) throws NotRegisteredException;

	abstract public NationsWorld getWorld(String name) throws NotRegisteredException;

	abstract public List<NationsWorld> getWorlds();

	abstract public NationsWorld getTownWorld(String townName);

	abstract public void removeResident(Resident resident);

	abstract public void removeTownBlock(TownBlock townBlock);

	abstract public void removeTownBlocks(Town town);
	
	abstract public void removeNationBlock(NationBlock nationBlock);

	abstract public void removeNationBlocks(Nation nation);
	
	abstract public void removeAllianceBlock(AllianceBlock allianceBlock);

	abstract public void removeAllianceBlocks(Alliance alliance);

	abstract public List<TownBlock> getAllTownBlocks();
	
	abstract public List<NationBlock> getAllNationBlocks();
	
	abstract public List<AllianceBlock> getAllAllianceBlocks();

	abstract public void newResident(String name) throws AlreadyRegisteredException, NotRegisteredException;

	abstract public void newTown(String name) throws AlreadyRegisteredException, NotRegisteredException;

	abstract public void newNation(String name) throws AlreadyRegisteredException, NotRegisteredException;
	
	abstract public void newAlliance(String name) throws AlreadyRegisteredException, NotRegisteredException;

	abstract public void newSiegeZone(String attackingNationName, String defendingTownName) throws AlreadyRegisteredException;

	abstract public void newWorld(String name) throws AlreadyRegisteredException;

	abstract public void removeTown(Town town);

	abstract public void removeTown(Town town, boolean delayFullRemoval);
	
	abstract public void removeNation(Nation nation);

	abstract public void removeNation(Nation nation, boolean delayFullRemoval);

	public abstract void removeSiege(Siege siege);

	public abstract void removeSiegeZone(SiegeZone siegeZone);

	abstract public void removeWorld(NationsWorld world) throws UnsupportedOperationException;

	abstract public Set<String> getResidentKeys();

	abstract public Set<String> getTownsKeys();

	abstract public Set<String> getNationsKeys();
	
	abstract public Set<String> getAlliancesKeys();

	abstract public Set<String> getSiegeZonesKeys();

	abstract public List<Town> getTownsWithoutNation();

	abstract public List<Resident> getResidentsWithoutTown();
	
	abstract public List<Nation> getNationsWithoutAlliance();

	abstract public void renameTown(Town town, String newName) throws AlreadyRegisteredException, NotRegisteredException;

	abstract public void renameNation(Nation nation, String newName) throws AlreadyRegisteredException, NotRegisteredException;
	
	abstract public void renameAlliance(Alliance alliance, String newName) throws AlreadyRegisteredException, NotRegisteredException;
	
	abstract public void mergeNation(Nation succumbingNation, Nation prevailingNation) throws AlreadyRegisteredException, NotRegisteredException;

	abstract public void renamePlayer(Resident resident, String newName) throws AlreadyRegisteredException, NotRegisteredException;

	abstract public void renameGroup(PlotGroup group, String newName) throws AlreadyRegisteredException;

	abstract public void removeTownFromNation(Main plugin, Town town, Nation nation);

	abstract public void addTownToNation(Main plugin, Town town,Nation nation);
	
	abstract public void removeNationFromAlliance(Main plugin, Nation nation, Alliance alliance);

	abstract public void addNationToAlliance(Main plugin, Nation nation, Alliance alliance);
}
