package com.steffbeard.totalwar.nations;

import java.io.File;
import java.util.Arrays;

import com.steffbeard.totalwar.nations.utils.ConfigManager;

public class Config extends ConfigManager {
	
	/*
	 * got to add the @ConfigOptions later
	 */
	public boolean defaultPublic;
	public boolean defaultOpen;
	public boolean allowGriefing;
	public boolean allowRollback;
	public boolean warExplosions;
    public boolean realisticExplosions;
    public boolean isBossBar;
	public double pPlayer;
	public double pPlot;
	public double pKill;
	public double pKillPoints;
	public double pMayorKill;
	public double pKingKill;
	public double pBlock;
	public double pBlockPoints;
	public double declareCost;
	public double endCost;
	public int debrisChance;
	
	protected Config(final File dataFolder) {
        super(new File(dataFolder, "config.yml"), Arrays.asList("Nations Configuration"));
        	this.defaultPublic = true;
        	this.defaultOpen = false;
        	this.allowGriefing = true;
        	this.allowRollback = true;
        	this.warExplosions = true;
        	this.realisticExplosions = true;
	}
}
