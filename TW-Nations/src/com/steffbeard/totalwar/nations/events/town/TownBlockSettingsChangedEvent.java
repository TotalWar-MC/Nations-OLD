package com.steffbeard.totalwar.nations.events.town;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.steffbeard.totalwar.nations.objects.NationsWorld;
import com.steffbeard.totalwar.nations.objects.town.Town;
import com.steffbeard.totalwar.nations.objects.town.TownBlock;

public class TownBlockSettingsChangedEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	@Override
	public HandlerList getHandlers() {

		return handlers;
	}

	public static HandlerList getHandlerList() {

		return handlers;
	}

	private NationsWorld w;
	private Town t;
	private TownBlock tb;
	private TownBlockSettingsChangedEvent() {
		super(!Bukkit.getServer().isPrimaryThread());
	}
	
	
	public TownBlockSettingsChangedEvent (NationsWorld w) {
		this();
		this.w = w;
	}

	public TownBlockSettingsChangedEvent (Town t) {
		this();
		this.t = t;
	}

	public TownBlockSettingsChangedEvent (TownBlock tb) {
		this();
		this.tb = tb;
	}
	
	public NationsWorld getNationsWorld() {
		return w;
	}
	
	public Town getTown() {
		return t;
	}
	
	public TownBlock getTownBlock() {
		return tb;
	}

}
