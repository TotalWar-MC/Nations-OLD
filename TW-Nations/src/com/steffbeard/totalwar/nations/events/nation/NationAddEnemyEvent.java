package com.steffbeard.totalwar.nations.events.nation;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.steffbeard.totalwar.nations.objects.nations.Nation;

public class NationAddEnemyEvent extends Event {
	
	private static HandlerList handlers = new HandlerList();

	private Nation enemy;
	private Nation nation;
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public NationAddEnemyEvent(Nation nation, Nation enemy) {
		super(!Bukkit.getServer().isPrimaryThread());
		this.enemy = enemy;
		this.nation = nation;
	}

	/**
	 *
	 * @return the nation that added the enemy.
	 */
	public Nation getNation() {
		return nation;
	}

	/**
	 *
	 * @return the nation that is now an enemy.
	 */
	public Nation getEnemy() {
		return enemy;
	}
}
