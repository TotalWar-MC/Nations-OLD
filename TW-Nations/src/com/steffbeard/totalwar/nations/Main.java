package com.steffbeard.totalwar.nations;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.steffbeard.totalwar.nations.config.Messages;
import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.economy.NationsEconomyHandler;
import com.steffbeard.totalwar.nations.exceptions.NationsException;
import com.steffbeard.totalwar.nations.exceptions.NotRegisteredException;
import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.util.BukkitTools;
import com.steffbeard.totalwar.nations.util.coord.Coord;
import com.steffbeard.totalwar.nations.util.coord.WorldCoord;
import com.steffbeard.totalwar.nations.util.player.PlayerCache;

public class Main extends JavaPlugin {
	private static final Logger LOGGER = LogManager.getLogger(Main.class);
	private String version = "2.0.0";
	
	private static Main plugin;
	
	private NationsUniverse nationsUniverse;

	private Map<String, PlayerCache> playerCache = Collections.synchronizedMap(new HashMap<>());

	private Essentials essentials = null;
	
	private boolean citizens2 = false;
	public static boolean isSpigot = false;
	private boolean error = false;
	
	public Main() {
		plugin = this;
	}
	
	@Override
	public void onEnable() {
		isSpigot = BukkitTools.isSpigot();
	}

	private void checkPlugins() {

		List<String> using = new ArrayList<>();
		Plugin test;

		if (Settings.isUsingEconomy()) {

			if (NationsEconomyHandler.setupEconomy()) {
				using.add(NationsEconomyHandler.getVersion());
				if (NationsEconomyHandler.getVersion().startsWith("Essentials Economy")) {
					System.out.println("[Nations] Warning: Essentials Economy has been known to reset town and nation bank accounts to their default amount. The authors of Essentials recommend using another economy plugin until they have fixed this bug.");
				}
					
			} else {
				Messages.sendErrorMsg("No compatible Economy plugins found. Install Vault.jar with any of the supported eco systems.");
				Messages.sendErrorMsg("If you do not want an economy to be used, set using_economy: false in your Nations config.yml.");
			}
		}
		
		/*
		 * Test for Citizens2 so we can avoid removing their NPC's
		 */
		test = getServer().getPluginManager().getPlugin("Citizens");
		if (test != null) {
			if (getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
				citizens2 = test.getDescription().getVersion().startsWith("2");
			}
		}
	
	}

	/**
	 * Fetch the NationsUniverse instance.
	 * 
	 * @return NationsUniverse
	 * @deprecated use {@link com.palmergames.bukkit.towny.NationsUniverse#getInstance()}
	 */
	public com.steffbeard.totalwar.nations.NationsUniverse getNationsUniverse() {

		return nationsUniverse;
	}

	public String getVersion() {

		return version;
	}

	/**
	 * @return the error
	 */
	public boolean isError() {

		return error;
	}

	/**
	 * @param error the error to set
	 */
	protected void setError(boolean error) {

		this.error = error;
	}

	// is Essentials active
	public boolean isEssentials() {

		return (Settings.isUsingEssentials() && (this.essentials != null));
	}

	// is Citizens2 active
	public boolean isCitizens2() {

		return citizens2;
	}

	/**
	 * @return Essentials object
	 * @throws NationsException - If Nations can't find Essentials.
	 */
	public Essentials getEssentials() throws NationsException {

		if (essentials == null)
			throw new NationsException("Essentials is not installed, or not enabled!");
		else
			return essentials;
	}

	public World getServerWorld(String name) throws NotRegisteredException {

		for (World world : BukkitTools.getWorlds())
			if (world.getName().equals(name))
				return world;

		throw new NotRegisteredException(String.format("A world called '$%s' has not been registered.", name));
	}

	public boolean hasCache(Player player) {

		return playerCache.containsKey(player.getName().toLowerCase());
	}

	public void newCache(Player player) {

		try {
			playerCache.put(player.getName().toLowerCase(), new PlayerCache(NationsUniverse.getInstance().getDataSource().getWorld(player.getWorld().getName()), player));
		} catch (NotRegisteredException e) {
			Messages.sendErrorMsg(player, "Could not create permission cache for this world (" + player.getWorld().getName() + ".");
		}

	}

	public void deleteCache(Player player) {

		deleteCache(player.getName());
	}

	public void deleteCache(String name) {

		playerCache.remove(name.toLowerCase());
	}

	/**
	 * Fetch the current players cache
	 * Creates a new one, if one doesn't exist.
	 * 
	 * @param player - Player to get the current cache from.
	 * @return the current (or new) cache for this player.
	 */
	public PlayerCache getCache(Player player) {

		if (!hasCache(player)) {
			newCache(player);
			getCache(player).setLastTownBlock(new WorldCoord(player.getWorld().getName(), Coord.parseCoord(player)));
		}

		return playerCache.get(player.getName().toLowerCase());
	}

	/**
	 * Resets all Online player caches, retaining their location info.
	 */
	public void resetCache() {

		for (Player player : BukkitTools.getOnlinePlayers())
			if (player != null)
				getCache(player).resetAndUpdate(new WorldCoord(player.getWorld().getName(), Coord.parseCoord(player))); // Automatically
																														// resets
																														// permissions.
	}

	/**
	 * Resets all Online player caches if their location equals this one
	 * 
	 * @param worldCoord - the location to check for
	 */
	public void updateCache(WorldCoord worldCoord) {

		for (Player player : BukkitTools.getOnlinePlayers())
			if (player != null)
				if (Coord.parseCoord(player).equals(worldCoord))
					getCache(player).resetAndUpdate(worldCoord); // Automatically
																	// resets
																	// permissions.
	}

	/**
	 * Resets all Online player caches if their location has changed
	 */
	public void updateCache() {

		WorldCoord worldCoord = null;

		for (Player player : BukkitTools.getOnlinePlayers()) {
			if (player != null) {
				worldCoord = new WorldCoord(player.getWorld().getName(), Coord.parseCoord(player));
				PlayerCache cache = getCache(player);
				if (cache.getLastTownBlock() != worldCoord)
					cache.resetAndUpdate(worldCoord);
			}
		}
	}

	/**
	 * Resets a specific players cache if their location has changed
	 * 
	 * @param player - Player, whose cache is to be updated.
	 */
	public void updateCache(Player player) {

		WorldCoord worldCoord = new WorldCoord(player.getWorld().getName(), Coord.parseCoord(player));
		PlayerCache cache = getCache(player);

		if (cache.getLastTownBlock() != worldCoord)
			cache.resetAndUpdate(worldCoord);
	}

	/**
	 * Resets a specific players cache
	 * 
	 * @param player - Player, whose cache is to be reset.
	 */
	public void resetCache(Player player) {

		getCache(player).resetAndUpdate(new WorldCoord(player.getWorld().getName(), Coord.parseCoord(player)));
	}

	public void setPlayerMode(Player player, String[] modes, boolean notify) {

		if (player == null)
			return;

		try {
			Resident resident = NationsUniverse.getInstance().getDataSource().getResident(player.getName());
			resident.setModes(modes, notify);

		} catch (NotRegisteredException e) {
			// Resident doesn't exist
		}
	}

	/**
	 * Remove ALL current modes (and set the defaults)
	 * 
	 * @param player - player, whose modes are to be reset (all removed).
	 */
	public void removePlayerMode(Player player) {

		try {
			Resident resident = NationsUniverse.getInstance().getDataSource().getResident(player.getName());
			resident.clearModes();

		} catch (NotRegisteredException e) {
			// Resident doesn't exist
		}

	}

	/**
	 * Fetch a list of all the players current modes.
	 * 
	 * @param player - player, whose modes are to be listed, taken.
	 * @return list of modes
	 */
	public List<String> getPlayerMode(Player player) {

		return getPlayerMode(player.getName());
	}

	public List<String> getPlayerMode(String name) {

		try {
			Resident resident = NationsUniverse.getInstance().getDataSource().getResident(name);
			return resident.getModes();

		} catch (NotRegisteredException e) {
			// Resident doesn't exist
			return null;
		}
	}

	/**
	 * Check if the player has a specific mode.
	 * 
	 * @param player - Player to be checked
	 * @param mode - Mode to be checked for within player.
	 * @return true if the mode is present.
	 */
	public boolean hasPlayerMode(Player player, String mode) {

		return hasPlayerMode(player.getName(), mode);
	}

	public boolean hasPlayerMode(String name, String mode) {

		try {
			Resident resident = NationsUniverse.getInstance().getDataSource().getResident(name);
			return resident.hasMode(mode);

		} catch (NotRegisteredException e) {
			// Resident doesn't exist
			return false;
		}
	}

	public String getConfigPath() {

		return getDataFolder().getPath() + File.separator + "settings" + File.separator + "config.yml";
	}

	public Object getSetting(String root) {

		return Settings.getProperty(root);
	}

	public void log(String msg) {

		if (Settings.isLogging()) {
			LOGGER.info(ChatColor.stripColor(msg));
		}
	}
	

	public boolean parseOnOff(String s) throws Exception {

		if (s.equalsIgnoreCase("on"))
			return true;
		else if (s.equalsIgnoreCase("off"))
			return false;
		else
			throw new Exception(String.format(Settings.getLangString("msg_err_invalid_input"), " on/off."));
	}

	/**
	 * @return the Nations instance
	 */
	public static Main getPlugin() {
		return plugin;
	}
}
