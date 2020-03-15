package com.steffbeard.totalwar.nations.misc.trails;

import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class CustomEvents
{
    private Plugin plugin;
    private boolean blockIntrctLstnersEnbled;
    private boolean useSignificantMoveEvent;
    
    public CustomEvents(final Plugin plugin, final boolean interactListenersEnabled, final boolean significantMoveEventEnabled) {
        this.plugin = plugin;
        this.blockIntrctLstnersEnbled = interactListenersEnabled;
        this.useSignificantMoveEvent = significantMoveEventEnabled;
    }
    
    public void initializeLib() {
        if (this.blockIntrctLstnersEnbled) {
            final BlockInteractListener listener = new BlockInteractListener(this.plugin);
            this.plugin.getServer().getPluginManager().registerEvents((Listener)listener, this.plugin);
        }
        if (this.useSignificantMoveEvent) {
            final PlayerMoveEventTask task = new PlayerMoveEventTask();
            task.runTaskTimer(this.plugin, 20L, 3L);
        }
    }
}
