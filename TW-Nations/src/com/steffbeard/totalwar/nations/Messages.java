package com.steffbeard.totalwar.nations;

import java.io.File;
import java.util.Arrays;

import org.bukkit.ChatColor;

import com.steffbeard.totalwar.nations.utils.Skyoconfig;

public class Messages extends Skyoconfig {
	
    @ConfigOptions(name = "messages.prefix")
    public String prefix;
    @ConfigOptions(name = "messages.permission")
    public String messagePermission;
    @ConfigOptions(name = "messages.1")
    public String message1;
    @ConfigOptions(name = "messages.2")
    public String message2;
    @ConfigOptions(name = "messages.3")
    public String message3;
    @ConfigOptions(name = "messages.4")
    public String message4;
    @ConfigOptions(name = "messages.5")
    public String message5;
    @ConfigOptions(name = "messages.6")
    public String message6;
    
    protected Messages(final File dataFolder) {
        super(new File(dataFolder, "messages.yml"), Arrays.asList("War messages"));
        this.prefix = ChatColor.RED + "[War]";
        this.messagePermission = ChatColor.RED + "You do not have the permission to perform this action.";
        this.message1 = ChatColor.GREEN + "Padlock placed ! If you want to remove it, you have to break this block.";
        this.message2 = ChatColor.GOLD + "Padlock removed.";
        this.message3 = ChatColor.RED + "This block has a padlock.";
        this.message4 = ChatColor.GREEN + "Padlock finder enabled ! Your compasses will now point to its location. You can reset it back to the spawn by doing another right click with any padlock finder.";
        this.message5 = ChatColor.RED + "Padlock finder disabled.";
        this.message6 = ChatColor.RED + "You can't place this key in this chest.";
    }
}
