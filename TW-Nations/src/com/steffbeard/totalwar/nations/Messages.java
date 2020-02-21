package com.steffbeard.totalwar.nations;

import java.io.File;
import java.util.Arrays;

import com.steffbeard.totalwar.nations.utils.ConfigManager;

public class Messages extends ConfigManager {
	 
	protected Messages(final File dataFolder) {
	        super(new File(dataFolder, "messages.yml"), Arrays.asList("Messages"));
	}
}
