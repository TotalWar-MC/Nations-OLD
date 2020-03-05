package com.steffbeard.totalwar.nations.event;

import com.palmergames.bukkit.towny.object.Nation;
import com.steffbeard.totalwar.nations.objects.Alliance;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class AllianceAddNationEvent extends Event  {

    private static final HandlerList handlers = new HandlerList();
    
    private Alliance alliance;
    private Nation nation;

    @Override
    public HandlerList getHandlers() {
    	
        return handlers;
    }
    
    public static HandlerList getHandlerList() {

		return handlers;
	}

    public AllianceAddNationEvent(Nation nation, Alliance alliance) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.alliance = alliance;
        this.nation = nation;
    }

    /**
     *
     * @return the town who has joined a nation.
     */
    public Nation getNation() {
        return nation;
    }

    /**
     *
     * @return the nation the town has just joined.
     */
    public Alliance getAlliance() {
        return alliance;
    }
    
}