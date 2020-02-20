package com.steffbeard.totalwar.nations;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.steffbeard.totalwar.nations.Config;

public class Main extends JavaPlugin {

	private static Config config;
	
	@Override
	public void onEnable() {
/*
 *  Loads files such as 
 *  config.yml
 *  alliances.yml
 *  
 */
	File a = new File("alliances.yml");

	if(!a.exists()) {
		try {
			a.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arges) {
		return false;
	
	}

}
