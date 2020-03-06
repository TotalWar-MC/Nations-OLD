package com.steffbeard.totalwar.nations;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;

import com.steffbeard.totalwar.nations.utils.ConfigManager;

public class Config extends ConfigManager {
	
	/*
	 * got to add the @ConfigOptions later
	 */
	public boolean defaultPublic;
	public boolean defaultOpen;
	public static boolean allowGriefing;
	public boolean allowRollback;
	public static boolean warExplosions;
    public boolean realisticExplosions;
    public boolean recordBlockBreak;
    public boolean recordBlockPlaced; // create config that asks if they would also like to record blockPlaces
    public boolean recordEntityExplode;
    public boolean recordBlockExplode;
    public boolean recordBlockBurn;
    public boolean recordBlockIgnite;
    public boolean recordBlockFromTo;
    public boolean recordPlayerBucketEmpty;
    public boolean useListeners;
    public boolean debugMessages;
	public static double pPlayer;
	public static double pPlot;
	public double pKill;
	public double pKillPoints;
	public double pMayorKill;
	public double pKingKill;
	public double pBlock;
	public double pBlockPoints;
	public static double declareCost;
	public static double endCost;
	public int debrisChance;
	public int timer;
	public List<String> worldBanList = new ArrayList<String>();
	public ArrayList<String> worldBlackList;
	public ArrayList<String> blockStringBlackList;
	public Set<Material> blockBlackList;
	
	protected Config(final File dataFolder) {
        super(new File(dataFolder, "config.yml"), Arrays.asList("Nations Configuration"));
        	this.defaultPublic = true;
        	this.defaultOpen = false;
        	Config.allowGriefing = true;
        	this.allowRollback = true;
        	Config.warExplosions = true;
        	this.realisticExplosions = true;
	}
}
