package com.steffbeard.totalwar.nations.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AllianceTagChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private String newTag;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public AllianceTagChangeEvent(String newTag) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.newTag = newTag;
    }

    public String getNewTag() {
        return newTag;
    }
}