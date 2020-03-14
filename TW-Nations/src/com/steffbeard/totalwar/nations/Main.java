package com.steffbeard.totalwar.nations;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.steffbeard.totalwar.nations.listeners.BlockEventListener;
import com.steffbeard.totalwar.nations.listeners.EnemyWalkWWar;
import com.steffbeard.totalwar.nations.listeners.GriefListener;
import com.steffbeard.totalwar.nations.listeners.NationWalkEvent;
import com.steffbeard.totalwar.nations.listeners.PlayerListener;
import com.steffbeard.totalwar.nations.listeners.PvPListener;
import com.steffbeard.totalwar.nations.listeners.WarListener;
import com.steffbeard.totalwar.nations.managers.GriefManager;
import com.steffbeard.totalwar.nations.managers.WarManager;
import com.steffbeard.totalwar.nations.objects.Alliance;
import com.steffbeard.totalwar.nations.objects.War;
import com.steffbeard.totalwar.nations.tasks.SaveTask;
import com.steffbeard.totalwar.nations.trades.TradeFile;
import com.steffbeard.totalwar.nations.utils.ResidentUtils;
import com.steffbeard.totalwar.nations.utils.SBlock;
import com.steffbeard.totalwar.nations.Messages;
import com.steffbeard.totalwar.nations.commands.TradeCommand;
import com.steffbeard.totalwar.nations.commands.WarCommand;
import com.steffbeard.totalwar.nations.Config;
import com.steffbeard.totalwar.nations.data.Alliances;
import com.steffbeard.totalwar.nations.data.DataAccessor;

public class Main extends JavaPlugin {
	
	public Main plugin;
	private Logger logger;
	protected Config config;
    protected Messages messages;
    protected Alliances alliances;
    private TradeFile tradeFile;
    private DataAccessor blockdata;
    private GriefManager gm;
    public static boolean isBossBar = false;
    
    private Set<SBlock> blocksBroken = new HashSet<SBlock>();
    private Set<Material> banList = new HashSet<Material>();
    
    public static HashMap<Chunk, List<Location>> wallBlocks = new HashMap<Chunk, List<Location>>();
    public static List<String> messagedPlayers = new ArrayList<String>();
    public Map<String, ResidentUtils> allResidents = new HashMap<String, ResidentUtils>();
    
    //set up the date conversion spec and the character set for file writing
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd zzz HH:mm:ss");
    private static final Charset utf8 = StandardCharsets.UTF_8;
    
    private static final String deathsFile="deaths.txt";
    public static File idConfigFile = null;
    public static YamlConfiguration idConfig = null;
    File wallConfigFile = new File(this.getDataFolder(), "walls.yml");
    
    public Main getInstance() {
		  return plugin;
	  }
    
	@Override
	public void onEnable() {
		plugin = this;
		this.logger = getLogger();
		this.blockdata = new DataAccessor(this);
		this.blockdata.initializeConfig();
		this.blockdata.loadData();
		
		final File dataFolder = this.getDataFolder();
        this.config = new Config(dataFolder);
        try {
            this.config.load();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.messages = new Messages(dataFolder);
        try {
            this.messages.load();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        /*
         * Register Listeners
         */
        final PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents((Listener)new PlayerListener(plugin), (Plugin)this);
        manager.registerEvents((Listener)new GriefListener(plugin, null), (Plugin)this);
        manager.registerEvents((Listener)new WarListener(), (Plugin)this);
        manager.registerEvents((Listener)new PvPListener(), (Plugin)this);
        manager.registerEvents((Listener)new NationWalkEvent(), (Plugin)this);
        manager.registerEvents((Listener)new EnemyWalkWWar(), (Plugin)this);
 // 	manager.registerEvents((Listener)new BlockEventListener(plugin, this.blocksBroken, banList), (Plugin)this);
        getCommand("war").setExecutor(new WarCommand());
        getCommand("trade").setExecutor(new TradeCommand());
        
        if(Bukkit.getPluginManager().getPlugin("BossBarAPI")!=null){
        	isBossBar = true;
        }
        for(Town town : TownyUniverse.getDataSource().getTowns()){
        	town.setAdminEnabledPVP(false);
        	town.setAdminDisabledPVP(false);
        	town.setPVP(false);
        }
        
        for (War w : WarManager.getWars()) {
            for (Alliance alliance : w.getAlliancessInWar()) {
            	for (Nation n : alliance.getNations()) {
                for (Town t : nation.getTowns()) {
                  t.setPVP(true);
                }
            }
          }

          Main.getDataSource().saveTowns();

          this.saveDefaultConfig();

          if(!(wallConfigFile.exists())){
      	      try {
      	    	  wallConfigFile.createNewFile();
      		} catch (IOException e) {

      			e.printStackTrace();
      		}
          } if (!idConfigFile.exists()) {
            try
            {
                idConfigFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }  idConfig = YamlConfiguration.loadConfiguration(idConfigFile);
            try
            {
                idConfig.save(idConfigFile);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            if(config.allowRollback){
            	new SaveTask(this.gm).runTaskTimer(plugin, config.timer, config.timer);
            }
            try{
            	for (Resident re : tUniverse.getActiveResidents()){
            		if (allResidents.get(re.getName())==null){
            			addResident(re.getName());
            		}
            	}
            }catch (Exception ex)
            {
                System.out.println("failed to add residents!");
                ex.printStackTrace();
            }
        
        this.logger.info("> TW-NATIONS IS ONLINE");
	}
	
	@Override
	public void onDisable() {
		this.blockdata.saveData();
	}
	
	public void onReload() {
		this.onDisable();
		this.onEnable();
	}
	
	public void setBlocksBroken(Set<SBlock> blocksBroken){
		  this.blocksBroken = new HashSet<SBlock>();
		  for(SBlock block : blocksBroken){
			  if(!this.banList.contains(block.getType())){
				  this.blocksBroken.add(block);		  
			  }
		  }
	  }
	  
/*
 *  Returns a Set<org.bukkit.Block> representative of all the blocks altered by destruction 
 */
	
	public Set<SBlock> getBlocksBroken() {
		return this.blocksBroken;
	}
	  
	public boolean addToList(SBlock sb) {
		if(!this.banList.contains(sb.getType())){
			return this.blocksBroken.add(sb);
		}
		return false;
	}
	  
	public boolean addToList(Block b){
		if(!this.banList.contains(b.getType())){
			return this.blocksBroken.add(new SBlock(b));
		}
		return false;  
	}
	
	public boolean addToList(Location loc){
		if(!this.banList.contains(loc.getBlock().getType())){
			return this.blocksBroken.add(new SBlock(loc));
		}
		return false;  
	}
	
	public boolean removeFromList(SBlock sb){
		return this.blocksBroken.remove(sb);
	}
	
	public boolean removeFromList(Block b){
		return this.blocksBroken.remove(new SBlock(b));
	}
	  
	public boolean removeFromList(Location loc){
		return this.blocksBroken.remove(new SBlock(loc));
	}
	
	public void setBanList(Set<Material> banList){
		this.banList = banList;
	}
	  
	public Set<Material> getBanList(){
		return this.banList;
	}
	  
	/*
	 *  Returns true if the block types and locations match, ignoring entities in the BlockLocation, false if they do not 
	 * */
	public boolean containsBlockLocation(SBlock bl){
		for(SBlock blockLocation : this.blocksBroken){
			//if(blockLocation.type.equals("block")){
			if(blockLocation.mat.equals(bl.mat) && blockLocation.world.equals(bl.world) && blockLocation.x == bl.x && blockLocation.y == bl.y && blockLocation.z == bl.z){
				return true;
			}
			//} 
			/*if(blockLocation.type.equals("itemframe")){
				  if(blockLocation.x == bl.x && blockLocation.y == bl.y && blockLocation.z == bl.z){
					  return true;
				  }
			  }*/
		}
		return false;
	}
	  
	public SBlock getStoredSBlock(Block block){
		for(SBlock blockLocation : this.blocksBroken){
			if(blockLocation.world.equals(block.getWorld().getName().toString()) && blockLocation.x == block.getX() && blockLocation.y == block.getY() && blockLocation.z == block.getZ()){
				return blockLocation;
			} 
		}
		return null;
	}
	  
	public SBlock getStoredSBlock(Location loc){
		  for(SBlock blockLocation : this.blocksBroken){
			  if(blockLocation.world.equals(loc.getWorld().getName().toString()) && blockLocation.x == loc.getX() && blockLocation.y == loc.getY() && blockLocation.z == loc.getZ()){
				  return blockLocation;
			  } 
		  }
		  return null;
	}
	  
	public ArrayList<Block> getBlocksBrokenbyEntity(Entity e){
		ArrayList<Block> blocks = new ArrayList<Block>();
		for(SBlock blockLocation : this.blocksBroken){
			if(!blockLocation.ent.equals(null)){
				if(blockLocation.ent.equals(e.getUniqueId())){
					Location loc = new Location(Bukkit.getServer().getWorld(blockLocation.world), blockLocation.x, blockLocation.y, blockLocation.z);
					blocks.add(loc.getBlock());
				}
			}
		}
		return blocks;
	}
	  
	public ArrayList<SBlock> getSBlocksBrokenbyEntity(Entity e){
		ArrayList<SBlock> blocks = new ArrayList<SBlock>();
		for(SBlock blockLocation : this.blocksBroken){
			if(!blockLocation.ent.equals(null)){
				if(blockLocation.ent.equals(e.getUniqueId())){
					Location loc = new Location(Bukkit.getServer().getWorld(blockLocation.world), blockLocation.x, blockLocation.y, blockLocation.z);
					blocks.add(getStoredSBlock(loc));
				}
			}
		}
		return blocks;
	}
	
	public void addResident(String playerName){
		  ResidentUtils newPlayer = new ResidentUtils(playerName);
		  allResidents.put(playerName,newPlayer);
	  }

	  public ResidentUtils getResident(String playerName){
		  return allResidents.get(playerName);
	  }



	  // takes in information about the death that just happened and writes it to a file
	  public int writeKillRecord(long deathTime, String playerName, String killerName, String damageCause, String deathMessage){

			// convert the time in milliseconds to a date and then convert it to a string in a useful format (have to tack on the milliseconds)
			// format example: 2014-08-29 EDT 10:05:25:756
			Date deathDate = new Date(deathTime);
		    String deathDateString = format.format(deathDate)+":"+deathTime%1000;

		    if (killerName==null) {
		    	killerName="nonplayer";
		    }

		    // prepare the death record string that will be written to file
			List<String> deathRecord = Arrays.asList(deathDateString+": "+playerName+" died to "+killerName+" via "+damageCause+"; '"+deathMessage+"'");


			// append the death record to the specified file
			try {
					Files.write(Paths.get(deathsFile), deathRecord, utf8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			}
			// some kind of error occurred . . . .
			catch (IOException e) {
				e.printStackTrace();
				return 1;
			}
			// all good!
			return 0;
		}
	
	public Set<Material> convertBanList(List<String> banList2){
  		Set<Material> newBanList = new HashSet<Material>();
		if(!banList2.equals(null)){
			for(String s : banList2){
				Material mat = Material.valueOf(s.toUpperCase());
				if(!mat.equals(null)){
					newBanList.add(mat);
				}
			}
		}
		return newBanList;
	}
	
	public boolean atWar(Player p, Location loc){
		try
		{
			if(TownyUniverse.getDataSource().getResident(p.getName())!=null)
			{
				Resident re = TownyUniverse.getDataSource().getResident(p.getName());
				if(re.getTown()!=null){
					if(re.getTown().getNation()!=null){
						Nation nation = re.getTown().getNation();
						// add the player to the master list if they don't exist in it yet
						if (plugin.getResident(re.getName())==null){
							plugin.addResident(re.getName());
							System.out.println("resident added!");
						}
						War ww = WarManager.getWarForNation(nation);
						if (ww != null)
						{
							if(TownyUniverse.getTownBlock(loc)!=null){
								TownBlock townBlock = TownyUniverse.getTownBlock(loc);
								Town otherTown = townBlock.getTown();
								if(otherTown!=re.getTown()){
									if(otherTown.getNation()!=null){
										Nation otherNation = otherTown.getNation();
										if(otherNation!=nation){
											Set<Nation> nationsInWar = ww.getNationsInWar();
											if(nationsInWar.contains(otherNation)){
												//nations are at war with each other
												return true;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}catch (Exception ex) {
			return false;
		}
		return false;
	}
	
	public TradeFile getTradeFile() {
		return tradeFile;
	}
}