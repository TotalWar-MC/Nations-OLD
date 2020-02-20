package com.steffbeard.totalwar.nations;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.steffbeard.totalwar.nations.Config;

public class Main extends JavaPlugin {

	private Main plugin;
	private static Config config;
	
	@Override
	public void onEnable() {
	final File dataFolder = this.getDataFolder();
    config = new Config(dataFolder);
    try {
        config.load();
    }
    catch (Exception e) {
        e.printStackTrace();
    }
}
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arges) {
		return false;
	
	}

}
