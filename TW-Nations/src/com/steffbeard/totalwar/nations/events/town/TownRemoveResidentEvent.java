package com.steffbeard.totalwar.nations.events.town;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.objects.town.Town;

/**
 * Author: Chris H (Zren / Shade)
 * Date: 5/23/12
 *
 * Fired after a resident has been removed from a town.
 */
public class TownRemoveResidentEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    
    private Resident resident;
    private Town town;

    @Override
    public HandlerList getHandlers() {
    	
        return handlers;
    }
    
    public static HandlerList getHandlerList() {

		return handlers;
	}

    public TownRemoveResidentEvent(Resident resident, Town town) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.resident = resident;
        this.town = town;
    }

    /**
     *
     * @return the resident who has been removed from a town.
     */
    public Resident getResident() {
        return resident;
    }

    /**
     *
     * @return the town the resident was previously in.
     */
    public Town getTown() {
        return town;
    }

}
