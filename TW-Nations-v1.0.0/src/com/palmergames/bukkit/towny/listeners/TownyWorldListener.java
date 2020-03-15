// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.listeners;

import java.util.Iterator;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import java.util.Collection;
import com.palmergames.bukkit.towny.object.Coord;
import org.bukkit.block.BlockState;
import java.util.ArrayList;
import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.event.world.StructureGrowEvent;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.AlreadyRegisteredException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.Bukkit;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.WorldLoadEvent;
import java.util.List;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.event.Listener;

public class TownyWorldListener implements Listener
{
    private final Towny plugin;
    public static List<String> playersMap;
    
    public TownyWorldListener(final Towny instance) {
        this.plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldLoad(final WorldLoadEvent event) {
        this.newWorld(event.getWorld().getName());
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldInit(final WorldInitEvent event) {
        this.newWorld(event.getWorld().getName());
    }
    
    private void newWorld(final String worldName) {
        boolean dungeonWorld = false;
        if (Bukkit.getServer().getPluginManager().getPlugin("DungeonsXL") != null && worldName.startsWith("DXL_")) {
            dungeonWorld = true;
        }
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            townyUniverse.getDataSource().newWorld(worldName);
            final TownyWorld world = townyUniverse.getDataSource().getWorld(worldName);
            if (dungeonWorld) {
                world.setUsingTowny(false);
            }
            if (world == null) {
                TownyMessaging.sendErrorMsg("Could not create data for " + worldName);
            }
            else if (!dungeonWorld && !townyUniverse.getDataSource().loadWorld(world)) {
                townyUniverse.getDataSource().saveWorld(world);
            }
        }
        catch (AlreadyRegisteredException ex) {}
        catch (NotRegisteredException e) {
            TownyMessaging.sendErrorMsg("Could not create data for " + worldName);
            e.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onStructureGrow(final StructureGrowEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        if (!TownyAPI.getInstance().isTownyWorld(event.getWorld())) {
            return;
        }
        TownBlock townBlock = null;
        TownBlock otherTownBlock = null;
        Town town = null;
        Town otherTown = null;
        Resident resident = null;
        TownyWorld world = null;
        final List<BlockState> removed = new ArrayList<BlockState>();
        try {
            world = TownyUniverse.getInstance().getDataSource().getWorld(event.getWorld().getName());
        }
        catch (NotRegisteredException e) {
            return;
        }
        final Coord coord = Coord.parseCoord(event.getLocation());
        for (final BlockState blockState : event.getBlocks()) {
            final Coord blockCoord = Coord.parseCoord(blockState.getLocation());
            if (!world.hasTownBlock(blockCoord)) {
                continue;
            }
            if (coord.equals(blockCoord)) {
                continue;
            }
            if (world.hasTownBlock(coord)) {
                townBlock = TownyAPI.getInstance().getTownBlock(event.getLocation());
                if (townBlock.hasResident()) {
                    try {
                        resident = townBlock.getResident();
                    }
                    catch (NotRegisteredException ex) {}
                    otherTownBlock = TownyAPI.getInstance().getTownBlock(blockState.getLocation());
                    try {
                        if (otherTownBlock.hasResident() && otherTownBlock.getResident() != resident) {
                            removed.add(blockState);
                        }
                        else if (!otherTownBlock.hasResident()) {
                            removed.add(blockState);
                        }
                        else {
                            if (resident == otherTownBlock.getResident()) {
                                continue;
                            }
                            continue;
                        }
                    }
                    catch (NotRegisteredException ex2) {}
                }
                else {
                    try {
                        town = townBlock.getTown();
                    }
                    catch (NotRegisteredException ex3) {}
                    try {
                        otherTownBlock = TownyAPI.getInstance().getTownBlock(blockState.getLocation());
                        otherTown = otherTownBlock.getTown();
                    }
                    catch (NotRegisteredException ex4) {}
                    if (town != otherTown) {
                        removed.add(blockState);
                    }
                    else if (otherTownBlock.hasResident()) {
                        removed.add(blockState);
                    }
                    else {
                        if (town == otherTown) {
                            continue;
                        }
                        continue;
                    }
                }
            }
            else {
                removed.add(blockState);
            }
        }
        if (!removed.isEmpty()) {
            event.getBlocks().removeAll(removed);
        }
    }
    
    static {
        TownyWorldListener.playersMap = new ArrayList<String>();
    }
}
