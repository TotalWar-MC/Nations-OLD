package com.steffbeard.totalwar.nations.data;

import java.io.File;
import java.util.Arrays;

import com.steffbeard.totalwar.nations.utils.ConfigManager;

public class Alliances extends ConfigManager {

	public Alliances(final File dataFolder) {
        super(new File(dataFolder, "alliances.yml"), Arrays.asList("Alliances List"));
	}
}
