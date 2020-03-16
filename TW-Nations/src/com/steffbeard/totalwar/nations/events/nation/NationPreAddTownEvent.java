package com.steffbeard.totalwar.nations.events.nation;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.steffbeard.totalwar.nations.objects.nations.Nation;
import com.steffbeard.totalwar.nations.objects.town.Town;

public class NationPreAddTownEvent extends Event implements Cancellable {

	private static final HandlerList handlers = new HandlerList();
	private String townName;
	private Town town;
	private String nationName;
	private Nation nation;
	private boolean isCancelled = false;
	private String cancelMessage = "Sorry this event was cancelled";

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public NationPreAddTownEvent(Nation nation, Town town) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.townName = town.getName();
		this.town = town;
		this.nation = nation;
		this.nationName = nation.getName();
	}

	public String getTownName() {
		return townName;
	}

	public String getNationName() {
		return nationName;
	}

	public Town getTown() {
		return town;
	}

	public Nation getNation() {
		return nation;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		isCancelled = cancelled;
	}

	public String getCancelMessage() {
		return cancelMessage;
	}

	public void setCancelMessage(String cancelMessage) {
		this.cancelMessage = cancelMessage;
	}
}
