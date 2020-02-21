package com.steffbeard.totalwar.nations.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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
    
    /***************************************
     * 
     * Make it so players within an alliance 
     * can not damage each other
     * 
     ***************************************
     */
    
    @EventHandler
    public void onDamage(final EntityDamageByEntityEvent e) {
        final Entity damaged = e.getEntity();
        final Entity damager = e.getDamager();
        //final Player p = (Player)e.getEntity();
      
        if (damaged instanceof Player && damager instanceof Player) {
            //final Player pdamaged = (Player)damaged;
            //final Player pdamager = (Player)damager;
            //if (members.contains(pdamaged.getDisplayName()) && members.contains(pdamager.getDisplayName())) {
            //    e.setCancelled(true);
            //    pdamager.sendMessage(String.valueOf(message.prefix) + "" + message.SAME_TEAM);
            //}
        }
    }
}
