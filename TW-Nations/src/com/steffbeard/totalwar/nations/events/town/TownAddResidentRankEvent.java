package com.steffbeard.totalwar.nations.events.town;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.objects.town.Town;

 /**
 * @author Artuto
 *
 * Fired after a Resident has been added to a Town rank.
 */
public class TownAddResidentRankEvent extends Event
{
	private static final HandlerList handlers = new HandlerList();
	
    private Resident resident;
    private String rank;
    private Town town;
    
    public TownAddResidentRankEvent(Resident resident, String rank, Town town) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.resident = resident;
        this.rank = rank;
        this.town = town;
    }
    
     /**
     *
     * @return the resident that got the rank
     * */
    public Resident getResident()
    {
        return resident;
    }
    
     /**
     *
     * @return the added rank
     * */
    public String getRank()
    {
        return rank;
    }
    
     /**
     *
     * @return the town this resident is part of
     * */
    public Town getTown()
    {
        return town;
    }

	@Override
	public HandlerList getHandlers() {

		return handlers;
	}
	
	public static HandlerList getHandlerList() {

		return handlers;
	}
}