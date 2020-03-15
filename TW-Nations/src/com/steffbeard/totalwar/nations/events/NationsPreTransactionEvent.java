package com.steffbeard.totalwar.nations.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.steffbeard.totalwar.nations.economy.NationsEconomyHandler;
import com.steffbeard.totalwar.nations.economy.Transaction;

public class NationsPreTransactionEvent extends Event implements Cancellable {
	private Transaction transaction;
	private static final HandlerList handlers = new HandlerList();
	private boolean isCancelled = false;
	private String cancelMessage = "Sorry this event was cancelled.";

	public NationsPreTransactionEvent(Transaction transaction) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.transaction = transaction;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.isCancelled = cancelled;
	}

	public String getCancelMessage() {
		return cancelMessage;
	}

	public static HandlerList getHandlerList() {

		return handlers;
	}

	public Transaction getTransaction() {
		return transaction;
	}
	
	public int getNewBalance() {
		switch (transaction.getType()) {
			case ADD:
				return (int) (NationsEconomyHandler.getBalance(transaction.getPlayer().getName(),
					transaction.getPlayer().getWorld())
					+ transaction.getAmount());
			case SUBTRACT:
				return (int) (NationsEconomyHandler.getBalance(transaction.getPlayer().getName(),
					transaction.getPlayer().getWorld())
					- transaction.getAmount());
			default:
				break;
		}
		
		return 0;
	}
}
