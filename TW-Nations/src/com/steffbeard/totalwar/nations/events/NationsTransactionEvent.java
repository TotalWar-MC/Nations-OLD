package com.steffbeard.totalwar.nations.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.steffbeard.totalwar.nations.economy.Transaction;

public class NationsTransactionEvent extends Event {
	
	private Transaction transaction;
	private static final HandlerList handlers = new HandlerList();
	
	public NationsTransactionEvent(Transaction transaction) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.transaction = transaction;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {

		return handlers;
	}

	public Transaction getTransaction() {
		return transaction;
	}
}
