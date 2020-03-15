package com.steffbeard.totalwar.nations.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.steffbeard.totalwar.nations.config.Messages;
import com.steffbeard.totalwar.nations.exceptions.NotRegisteredException;
import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.objects.town.Town;
import com.steffbeard.totalwar.nations.objects.town.TownBlock;
import com.steffbeard.totalwar.nations.objects.town.TownBlockOwner;
import com.steffbeard.totalwar.nations.permissions.Permission;

/**
 * @author Suneet Tipirneni (Siris)
 * A simple class which encapsulates the grouping of townblocks.
 */

/*
 * Have to add support for Alliance and Nation plots and blocks
 */
public class PlotGroup extends ObjectGroup implements TownBlockOwner {
	private Resident resident = null;
	private List<TownBlock> townBlocks;
	private double price = -1;
	private Town town;
	private Permission permissions;

	/**
	 * @param id   A unique identifier for the group id.
	 * @param name An alias for the id used for player in-game interaction via commands.
	 * @param town The town that this group is owned by.   
	 */
	public PlotGroup(UUID id, String name, Town town) {
		super(id, name);
		this.town = town;
	}

	/**
	 * Store plot group in format "name,id,town,price"
	 * @return The string in the format described.
	 */
	@Override
	public String toString() {
		return super.toString() + "," + getTown().toString() + "," + getPrice();
	}

	/**
	 * Override the name change method to internally rehash the plot group map.
	 * @param name The name of the group.
	 */
	@Override
	public void setName(String name) {
		if (getName() == null) {
			super.setName(name);
		}
		else {
			String oldName = getName();
			super.setName(name);
			town.renamePlotGroup(oldName, this);
		}
	}
	
	public void setTown(Town town) {
		this.town = town;
		
		try {
			town.addPlotGroup(this);
		} catch (Exception e) {
			Messages.sendErrorMsg(e.getMessage());
		}
	}
	
	public Town getTown() {
		return town;
	}

	/**
	 *
	 * @return The qualified resident mode string.
	 */
	public String toModeString() {
		return "Group{" + this.toString() + "}";
	}

	public double getPrice() {
		return price;
	}
	
	public void setResident(Resident resident) {
		if (hasResident())
			this.resident = resident;
	}

	public Resident getResident() throws NotRegisteredException {
		if (!hasResident())
			throw new NotRegisteredException("The Group " + this.toString() + "is not registered to a resident.");
		return resident;
	}

	public boolean hasResident() { return resident != null; }
	
	public void addTownBlock(TownBlock townBlock) {
		if (townBlocks == null)
			townBlocks = new ArrayList<>();
		
		townBlocks.add(townBlock);
	}

	public void removeTownBlock(TownBlock townBlock) {
		if (townBlocks != null)
			townBlocks.remove(townBlock);
	}

	@Override
	public void setTownblocks(List<TownBlock> townBlocks) {
		this.townBlocks = townBlocks;
	}

	public List<TownBlock> getTownBlocks() {
		return townBlocks;
	}

	@Override
	public boolean hasTownBlock(TownBlock townBlock) {
		return townBlocks.contains(townBlock);
	}

	public void setPrice(double price) {
		this.price = price;
	}
	
	public void addPlotPrice(double pPrice) {
		if (getPrice() == -1) {
			this.price = pPrice;
			return;
		}
		
		this.price += pPrice;
	}

	@Override
	public void setPermissions(String line) {
		this.permissions.load(line);
	}

	public Permission getPermissions() {
		return permissions;
	}

	public void setPermissions(Permission permissions) {
		this.permissions = permissions;
	}
	
}
