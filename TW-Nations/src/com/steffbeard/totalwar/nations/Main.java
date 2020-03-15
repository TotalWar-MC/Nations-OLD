package com.steffbeard.totalwar.nations;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.steffbeard.totalwar.nations.economy.NationsEconomyHandler;
import com.steffbeard.totalwar.nations.util.BukkitTools;

public class Main extends JavaPlugin {
	private static final Logger LOGGER = LogManager.getLogger(Main.class);
	private String version = "2.0.0";
	
	private static Main plugin;
	
	private boolean citizens2 = false;
	public static boolean isSpigot = false;
	
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
					System.out.println("[Towny] Warning: Essentials Economy has been known to reset town and nation bank accounts to their default amount. The authors of Essentials recommend using another economy plugin until they have fixed this bug.");
				}
					
			} else {
				Messages.sendErrorMsg("No compatible Economy plugins found. Install Vault.jar with any of the supported eco systems.");
				Messages.sendErrorMsg("If you do not want an economy to be used, set using_economy: false in your Towny config.yml.");
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
	// is Citizens2 active
	public boolean isCitizens2() {

		return citizens2;
	}
}
