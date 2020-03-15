package com.steffbeard.totalwar.nations.misc.trails;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Location;
import java.io.Serializable;

public class WrappedLocation implements Serializable
{
    private static final long serialVersionUID = -5944092517430475806L;
    private double x;
    private double y;
    private double z;
    private String world;
    private int blockX;
    private int blockY;
    private int blockZ;
    
    public WrappedLocation(final Location loc) {
        this.x = loc.getX();
        this.y = loc.getY();
        this.z = loc.getZ();
        this.blockX = loc.getBlockX();
        this.blockY = loc.getBlockY();
        this.blockZ = loc.getBlockZ();
        this.world = loc.getWorld().getUID().toString();
    }
    
    public int getBlockX() {
        return this.blockX;
    }
    
    public int getBlockY() {
        return this.blockY;
    }
    
    public int getBlockZ() {
        return this.blockZ;
    }
    
    public double getX() {
        return this.x;
    }
    
    public double getY() {
        return this.y;
    }
    
    public double getZ() {
        return this.z;
    }
    
    public World getWorld() {
        return Bukkit.getServer().getWorld(UUID.fromString(this.world));
    }
    
    public static boolean compareLocations(final WrappedLocation loc1, final Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX() && loc1.getBlockY() == loc2.getBlockY() && loc1.getBlockZ() == loc2.getBlockZ() && loc1.getWorld() == loc2.getWorld();
    }
}
