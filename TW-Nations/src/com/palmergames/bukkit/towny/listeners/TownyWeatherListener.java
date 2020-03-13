// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.listeners;

import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.weather.LightningStrikeEvent;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.event.Listener;

public class TownyWeatherListener implements Listener
{
    private final Towny plugin;
    
    public TownyWeatherListener(final Towny instance) {
        this.plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLightningStrike(final LightningStrikeEvent event) {
    }
    
    public Towny getPlugin() {
        return this.plugin;
    }
}
