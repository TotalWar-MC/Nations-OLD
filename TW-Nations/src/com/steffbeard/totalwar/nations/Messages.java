package com.steffbeard.totalwar.nations;

import java.io.File;
import java.util.Arrays;

import org.bukkit.ChatColor;

import com.steffbeard.totalwar.nations.utils.ConfigManager;

public class Messages extends ConfigManager {
	
	@ConfigOptions(name = "messages.prefix")
    public String prefix;
    @ConfigOptions(name = "messages.permission")
    public String messagePermission;
    @ConfigOptions(name = "messages.SAME_TEAM")
    public String SAME_TEAM;
	 
	protected Messages(final File dataFolder) {
        super(new File(dataFolder, "messages.yml"), Arrays.asList("Nations Messages"));
        this.prefix = ChatColor.GOLD + "[Nations]";
        this.SAME_TEAM = ChatColor.RED + "You can not attack members in the same alliance!";
	}
}
