package com.steffbeard.totalwar.nations.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.steffbeard.totalwar.nations.Main;
import com.steffbeard.totalwar.nations.Messages;



public class PlayerListener implements Listener {

	Main main;
	Messages message;
    
    public PlayerListener(final Main instance) {
        this.main = instance;
    }
    
    @EventHandler
    public void onDamage(final EntityDamageByEntityEvent e) {
    }
}
