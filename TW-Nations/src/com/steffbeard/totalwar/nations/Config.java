package com.steffbeard.totalwar.nations;

import java.io.File;
import java.util.Arrays;

import com.steffbeard.totalwar.nations.utils.Skyoconfig;

public class Config extends Skyoconfig {

	@ConfigOptions(name = "dp-per-player.int")
	public int dpPerPlayer;
	@ConfigOptions(name = "dp-per-plot.int")
	public int dpPerPlot;
	
	
	protected Config(final File dataFolder) {
        super(new File(dataFolder, "config.yml"), Arrays.asList("Nations configuration"));
        	this.dpPerPlayer = 1;
        	this.dpPerPlot = 3;
	}
}
