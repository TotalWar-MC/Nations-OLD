package com.steffbeard.totalwar.nations.util.coord;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import com.steffbeard.totalwar.nations.NationsUniverse;
import com.steffbeard.totalwar.nations.exceptions.NotRegisteredException;
import com.steffbeard.totalwar.nations.objects.NationsWorld;
import com.steffbeard.totalwar.nations.objects.town.TownBlock;

import java.util.Objects;

public class WorldCoord extends Coord {

	private String worldName;

	public WorldCoord(String worldName, int x, int z) {
		super(x, z);
		this.worldName = worldName;
	}

	public WorldCoord(String worldName, Coord coord) {
		super(coord);
		this.worldName = worldName;
	}

	public WorldCoord(WorldCoord worldCoord) {
		super(worldCoord);
		this.worldName = worldCoord.getWorldName();
	}

	public String getWorldName() {
		return worldName;
	}

	public Coord getCoord() {
		return new Coord(x, z);
	}

	@Deprecated
	public NationsWorld getWorld() throws NotRegisteredException {
		return getNationsWorld();
	}

	@Deprecated
	public WorldCoord(NationsWorld world, int x, int z) {
		super(x, z);
		this.worldName = world.getName();
	}

	@Deprecated
	public WorldCoord(NationsWorld world, Coord coord) {
		super(coord);
		this.worldName = world.getName();
	}

	public static WorldCoord parseWorldCoord(Entity entity) {
		return parseWorldCoord(entity.getLocation());
	}

	public static WorldCoord parseWorldCoord(Location loc) {
		return new WorldCoord(loc.getWorld().getName(), parseCoord(loc));
	}

	public static WorldCoord parseWorldCoord(Block block) {
		return new WorldCoord(block.getWorld().getName(), parseCoord(block.getX(), block.getZ()));
	}

	public WorldCoord add(int xOffset, int zOffset) {

		return new WorldCoord(getWorldName(), getX() + xOffset, getZ() + zOffset);
	}

	@Override
	public int hashCode() {

		int hash = 17;
		hash = hash * 27 + (worldName == null ? 0 : worldName.hashCode());
		hash = hash * 27 + x;
		hash = hash * 27 + z;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Coord))
			return false;

		if (!(obj instanceof WorldCoord)) {
			Coord that = (Coord) obj;
			return this.x == that.x && this.z == that.z;
		}

		WorldCoord that = (WorldCoord) obj;
		return this.x == that.x && this.z == that.z && (this.worldName == null ? that.worldName == null : this.worldName.equals(that.worldName));
	}

	@Override
	public String toString() {
		return worldName + "," + super.toString();
	}

	/**
	 * Shortcut for Bukkit.getWorld(worldName)
	 * 
	 * @return the relevant org.bukkit.World instance
	 */
	public World getBukkitWorld() {
		return Bukkit.getWorld(worldName);
	}

	/**
	 * Shortcut for NationsUniverse.getDataSource().getWorld(worldName)
	 * 
	 * @return the relevant NationsWorld instance
	 * @throws NotRegisteredException if unable to return a NationsWorld instance
	 */
	public NationsWorld getNationsWorld() throws NotRegisteredException {
		return NationsUniverse.getInstance().getDataSource().getWorld(worldName);
	}

	/**
	 * Shortcut for getNationsWorld().getTownBlock(getCoord())
	 * 
	 * @return the relevant TownBlock instance.
	 * @throws NotRegisteredException - If there is no TownBlock @ WorldCoord, then this exception.
	 */
	public TownBlock getTownBlock() throws NotRegisteredException {
		return getNationsWorld().getTownBlock(getCoord());
	}

	/**
	 * Checks that locations are in different cells without allocating any garbage to the heap.
	 * 
	 * @param from Original location
	 * @param to Next location
	 * @return whether the locations are in different cells
	 */
	public static boolean cellChanged(Location from, Location to) {
		return toCell(from.getBlockX()) != toCell(to.getBlockX()) ||
			   toCell(from.getBlockZ()) != toCell(to.getBlockZ()) ||
			   !Objects.equals(from.getWorld(), to.getWorld());
	}
}
