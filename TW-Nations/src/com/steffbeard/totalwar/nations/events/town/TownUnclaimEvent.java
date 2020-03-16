package com.steffbeard.totalwar.nations.events.town;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.steffbeard.totalwar.nations.objects.town.Town;
import com.steffbeard.totalwar.nations.util.coord.WorldCoord;


public class TownUnclaimEvent extends Event  {

    private static final HandlerList handlers = new HandlerList();
    
    private Town town;
    private WorldCoord worldCoord;

    @Override
    public HandlerList getHandlers() {
    	
        return handlers;
    }
    
    public static HandlerList getHandlerList() {

		return handlers;
	}

    public TownUnclaimEvent(Town _town, WorldCoord _worldcoord) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.town = _town;
        this.worldCoord = _worldcoord;
    }

    /**
     *
     * @return the Town.
     */
    public Town getTown() {
        return town;
    }
    
    /**
    *
    * @return the Unclaimed WorldCoord.
    *
    */
   public WorldCoord getWorldCoord() {
       return worldCoord;
   }
    
}