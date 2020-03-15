package com.steffbeard.totalwar.nations.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.steffbeard.totalwar.nations.economy.Transaction;

import org.bukkit.Bukkit;

public class TownTransactionEvent extends Event {
	private Town town;
	private static final HandlerList handlers = new HandlerList();
	private Transaction transaction;
	
	public TownTransactionEvent(Town town, Transaction transaction) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.town = town;
		this.transaction = transaction;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {

		return handlers;
	}

	public Town getTown() {
		return town;
	}

	public Transaction getTransaction() {
		return transaction;
	}
}
