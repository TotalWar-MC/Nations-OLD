package com.steffbeard.totalwar.nations;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.steffbeard.totalwar.nations.listeners.EnemyWalkWWar;
import com.steffbeard.totalwar.nations.listeners.GriefListener;
import com.steffbeard.totalwar.nations.listeners.NationWalkEvent;
import com.steffbeard.totalwar.nations.listeners.PlayerListener;
import com.steffbeard.totalwar.nations.listeners.PvPListener;
import com.steffbeard.totalwar.nations.listeners.WarListener;
import com.steffbeard.totalwar.nations.Messages;
import com.steffbeard.totalwar.nations.data.Alliances;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.steffbeard.totalwar.nations.Config;

public class Main extends JavaPlugin {
	
	public Main plugin;
	protected Config config;
    protected Messages messages;
    protected Alliances alliances;
    public static TownyUniverse tUniverse;
    public static Towny towny;
    
	@Override
	public void onEnable() {
		final File dataFolder = this.getDataFolder();
        this.config = new Config(dataFolder);
        try {
            this.config.load();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.messages = new Messages(dataFolder);
        try {
            this.messages.load();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.alliances = new Alliances(dataFolder);
        try {
        	this.alliances.load();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        /*
         * Register Listeners
         */
        final PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents((Listener)new PlayerListener(plugin), (Plugin)this);
        manager.registerEvents((Listener)new GriefListener(plugin, null), (Plugin)this);
        manager.registerEvents((Listener)new WarListener(), (Plugin)this);
        manager.registerEvents((Listener)new PvPListener(), (Plugin)this);
        manager.registerEvents((Listener)new NationWalkEvent(), (Plugin)this);
        manager.registerEvents((Listener)new EnemyWalkWWar(), (Plugin)this);
	}

}
