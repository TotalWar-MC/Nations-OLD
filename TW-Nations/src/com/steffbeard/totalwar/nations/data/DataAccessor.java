package com.steffbeard.totalwar.nations.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.steffbeard.totalwar.nations.Main;
import com.steffbeard.totalwar.nations.utils.BlockSerialization;
import com.steffbeard.totalwar.nations.utils.SBlock;

public class DataAccessor{
	
	private Main plugin;
	private List<String> banList = new ArrayList<String>();
	public List<String> worldBanList = new ArrayList<String>();
	private List<String> tempWorldBanList = new ArrayList<String>();
	private Set<Material> banListConverted = new HashSet<Material>();
	public boolean useListeners;
	public boolean debugMessages;
	public boolean recordBlockBreak;
	public boolean recordBlockPlaced;
	public boolean recordEntityExplode;
	public boolean recordBlockExplode;
	public boolean recordBlockBurn;
	public boolean recordBlockIgnite;
	public boolean recordBlockFromTo;
	public boolean recordPlayerBucketEmpty;
	private Set<SBlock> blocksBroken = new HashSet<SBlock>();
	private FileConfiguration blocks;
	private FileConfiguration f;
	private File bf;
	private File blocksFile; 
	
	public DataAccessor(Main plugin){
		this.plugin = plugin;
		this.bf = new File(this.plugin.getDataFolder().toString()+"/data");
		this.blocksFile = new File(bf, "blocks.yml");
	}
	
	public void initializeConfig() {
		banList = new ArrayList<String>();
		banListConverted = new HashSet<Material>();
		File file = new File(plugin.getDataFolder(), "config.yml");
		if(!file.exists()) {
			plugin.saveDefaultConfig();
	    }
	    f = YamlConfiguration.loadConfiguration(file);
	    this.useListeners = f.getBoolean("useListeners");
	    if(this.useListeners){
	    	this.recordBlockBreak = f.getBoolean("listeners.BlockBreak");
	    	this.recordBlockPlaced = f.getBoolean("listeners.BlockPlace");
	    	this.recordEntityExplode = f.getBoolean("listeners.EntityExplode");
	    	this.recordBlockExplode = f.getBoolean("listeners.BlockExplode");
	    	this.recordBlockBurn = f.getBoolean("listeners.BlockBurn");
	    	this.recordBlockIgnite = f.getBoolean("listeners.BlockIgnite");
	    	this.recordBlockFromTo = f.getBoolean("listeners.BlockFromTo");
	    	this.recordPlayerBucketEmpty = f.getBoolean("listeners.PlayerBucketEmpty");;
	    }else{
	    	this.recordBlockBreak = false;
	    	this.recordBlockPlaced = false;
	    	this.recordEntityExplode = false;
	    	this.recordBlockExplode = false;
	    	this.recordBlockBurn = false;
	    	this.recordBlockIgnite = false;
	    	this.recordBlockFromTo = false;
	    	this.recordPlayerBucketEmpty = false;
	    }	    
	    this.debugMessages = f.getBoolean("debugMessages");
	    this.tempWorldBanList = f.getStringList("blacklistWorlds");
	    if(!this.tempWorldBanList.isEmpty() && !this.tempWorldBanList.equals(null)){
	    	for(String s : this.tempWorldBanList){
	    		this.worldBanList.add(s.toLowerCase());
	    	}
	    }
	    this.banList = f.getStringList("blockBlacklist");
	    for(Material m : convertBanList(banList)){
	    	this.banListConverted.add(m);
	    }
	    plugin.setBanList(banListConverted);
	}
	
	public void loadData(){
		String listSerial = "";
		int size;
		bf = new File(plugin.getDataFolder().toString() + "/data");
		blocksFile = new File(bf, "blocks.yml");  
		if (!bf.exists()) {
			Bukkit.getServer().getLogger().info("Directory Doesn't Exist, Creating...");
		    bf.mkdir();
		}
		if (!blocksFile.exists()) {
			Bukkit.getServer().getLogger().info("Default blockdata file Doesn't Exist, Creating...");
		    plugin.saveResource("data/blocks.yml", false);
		}else{
			blocks = YamlConfiguration.loadConfiguration(blocksFile);
			if(blocks!=null){
				if(!(blocks.getString("blocks") == "") && (blocks.getString("blocks") != null)){
					listSerial = blocks.getString("blocks");
					size = blocks.getInt("size");
					try {
						if(!listSerial.equals("") && !listSerial.equals(null)){
							blocksBroken = BlockSerialization.fromBase64(listSerial, size);
							plugin.setBlocksBroken(blocksBroken);
						}			
					} catch (IOException e) {
						Bukkit.getServer().getLogger().info("Can't load BlocksFile");
						e.printStackTrace();
					}
				}		
			}
		}	
	}
	
	public void saveData(){
		int size;
		blocks = YamlConfiguration.loadConfiguration(blocksFile);
		if(plugin.getBlocksBroken().isEmpty()){
			size = 0;
		}else{
			size = plugin.getBlocksBroken().size();
		}
		if(plugin.getBlocksBroken()!=null){
			String listSerial = BlockSerialization.toBase64(plugin.getBlocksBroken());
			blocks.set("blocks", listSerial);
			blocks.set("size", size);
		}
		if (!bf.exists()){
			bf.mkdir();
	    }
		if(blocksFile.exists())
			blocksFile.delete();
		try {
			blocks.save(blocksFile);
		} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
		if (!blocksFile.exists()){
			try {
				blocksFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Set<Material> convertBanList(List<String> banList2){
		Set<Material> newBanList = new HashSet<Material>();
		if(!banList2.equals(null)){
			for(String s : banList2){
				Material mat = Material.valueOf(s);
				if(!mat.equals(null)){
					newBanList.add(mat);
				}
				/*EntityType et = EntityType.valueOf(s);
				if(!et.equals(null)){
					newBanList.add(et);
				}*/
			}
		}
		return newBanList;
	}
}