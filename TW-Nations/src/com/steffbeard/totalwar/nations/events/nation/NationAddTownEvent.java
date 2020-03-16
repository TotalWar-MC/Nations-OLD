package com.steffbeard.totalwar.nations.events.nation;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.steffbeard.totalwar.nations.objects.nations.Nation;
import com.steffbeard.totalwar.nations.objects.town.Town;


public class NationAddTownEvent extends Event  {

    private static final HandlerList handlers = new HandlerList();
    
    private Town town;
    private Nation nation;

    @Override
    public HandlerList getHandlers() {
    	
        return handlers;
    }
    
    public static HandlerList getHandlerList() {

		return handlers;
	}

    public NationAddTownEvent(Town town, Nation nation) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.town = town;
        this.nation = nation;
    }

    /**
     *
     * @return the town who has joined a nation.
     */
    public Town getTown() {
        return town;
    }

    /**
     *
     * @return the nation the town has just joined.
     */
    public Nation getNation() {
        return nation;
    }
    
}
