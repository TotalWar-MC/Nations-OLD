package com.steffbeard.totalwar.nations.objects;

import java.io.File;

public class Alliance {
	
	public Alliance() {
		File a = new File("alliances.yml");
		
		if(!a.exists()) {
			try {
				a.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	public boolean callAlly;
	//TODO
}
