package com.steffbeard.totalwar.nations;

import java.io.File;
import java.util.Arrays;

import org.bukkit.ChatColor;

import com.steffbeard.totalwar.nations.utils.ConfigManager;

public class Messages extends ConfigManager {
	
	//public static String msg_err_nation_sent_too_many_invites;
	//public static String msg_err_nation_has_too_many_requests;
	@ConfigOptions(name = "messages.prefix")
    public String prefix;
    @ConfigOptions(name = "messages.permission")
    public String messagePermission;
    @ConfigOptions(name = "messages.SAME_TEAM")
    public String SAME_TEAM;
    public static String tag_too_long;
    public static String msg_err_nation_doesnt_belong_to_any_alliance;
	 
	protected Messages(final File dataFolder) {
        super(new File(dataFolder, "messages.yml"), Arrays.asList("Nations Messages"));
        this.prefix = ChatColor.GOLD + "[Nations]";
        this.SAME_TEAM = ChatColor.RED + "You can not attack members in the same alliance!";
        Messages.tag_too_long = ChatColor.DARK_RED + "Alliance tag is too long!";
        Messages.msg_err_nation_doesnt_belong_to_any_alliance = ChatColor.DARK_RED + "This nation doesn't belong to an alliance!";
        //Messages.msg_err_nation_has_too_many_requests = ChatColor.DARK_RED + "Too many requests!";
        //Messages.msg_err_nation_sent_too_many_invites = ChatColor.DARK_RED + "Too many invites!";
	}
}
