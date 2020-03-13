// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.listeners;

import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import com.palmergames.bukkit.towny.object.PlayerCache;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.TownySettings;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.event.Listener;

public class TownyVehicleListener implements Listener
{
    private final Towny plugin;
    
    public TownyVehicleListener(final Towny instance) {
        this.plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVehicleDestroy(final VehicleDestroyEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        if (event.getAttacker() instanceof Player) {
            final Player player = (Player)event.getAttacker();
            boolean bBreak = true;
            Material vehicle = null;
            switch (event.getVehicle().getType()) {
                case MINECART: {
                    vehicle = Material.MINECART;
                    break;
                }
                case MINECART_FURNACE: {
                    vehicle = Material.POWERED_MINECART;
                    break;
                }
                case MINECART_HOPPER: {
                    vehicle = Material.HOPPER_MINECART;
                    break;
                }
                case MINECART_CHEST: {
                    vehicle = Material.STORAGE_MINECART;
                    break;
                }
                case MINECART_MOB_SPAWNER: {
                    vehicle = Material.MINECART;
                    break;
                }
                case MINECART_COMMAND: {
                    vehicle = Material.COMMAND_MINECART;
                    break;
                }
                case MINECART_TNT: {
                    vehicle = Material.EXPLOSIVE_MINECART;
                    break;
                }
            }
            if (vehicle != null && !TownySettings.isItemUseMaterial(vehicle.toString())) {
                return;
            }
            bBreak = PlayerCacheUtil.getCachePermission(player, event.getVehicle().getLocation(), vehicle, TownyPermission.ActionType.ITEM_USE);
            if (vehicle != null) {
                if (bBreak) {
                    return;
                }
                event.setCancelled(true);
                final PlayerCache cache = this.plugin.getCache(player);
                if (cache.hasBlockErrMsg()) {
                    TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
                }
            }
        }
    }
}
