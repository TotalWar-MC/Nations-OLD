package com.steffbeard.totalwar.nations.event;

import com.palmergames.bukkit.towny.object.Nation;
import com.steffbeard.totalwar.nations.objects.Alliance;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class AllianceRemoveNationEvent extends Event  {

    private static final HandlerList handlers = new HandlerList();
    
    private Nation nation;
    private Alliance alliance;

    @Override
    public HandlerList getHandlers() {
    	
        return handlers;
    }
    
    public static HandlerList getHandlerList() {

		return handlers;
	}

    public AllianceRemoveNationEvent(Nation nation, Alliance alliance) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.nation = nation;
        this.alliance = alliance;
    }

    /**
     *
     * @return the nation who has left an alliance.
     */
    public Nation getNation() {
        return nation;
    }

    /**
     *
     * @return the alliance the nation has just left.
     */
    public Alliance getAlliance() {
        return alliance;
    }
    
}