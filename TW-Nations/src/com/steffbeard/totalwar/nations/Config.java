package com.steffbeard.totalwar.nations;

import java.io.File;
import java.util.Arrays;

import com.steffbeard.totalwar.nations.utils.ConfigManager;

public class Config extends ConfigManager {
	
	public boolean defaultPublic;
	public boolean defaultOpen;
	
	protected Config(final File dataFolder) {
        super(new File(dataFolder, "config.yml"), Arrays.asList("Nations Configuration"));
        	this.defaultPublic = true;
        	this.defaultOpen = false;
	}
}
