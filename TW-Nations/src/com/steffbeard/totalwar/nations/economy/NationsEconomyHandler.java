/**
 * I need to remake this entirely to fit our own economy system and to ensure it works with Gringotts
 */

package com.steffbeard.totalwar.nations.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.steffbeard.totalwar.nations.Main;
import com.steffbeard.totalwar.nations.config.Messages;
import com.steffbeard.totalwar.nations.events.NationsPreTransactionEvent;
import com.steffbeard.totalwar.nations.events.NationsTransactionEvent;
import com.steffbeard.totalwar.nations.util.BukkitTools;

import java.util.UUID;

/**
 * Economy handler to interface with Register or Vault directly.
 * 
 * @author ElgarL
 * 
 */
@SuppressWarnings("deprecation")
public class NationsEconomyHandler {

	private static Main plugin = null;
	
	private static Economy vaultEconomy = null;

	private static EcoType Type = EcoType.NONE;

	private static String version = "";

	public enum EcoType {
		NONE, VAULT, RESERVE
	}

	public static void initialize(Main plugin) {

		NationsEconomyHandler.plugin = plugin;
	}

	/**
	 * @return the economy type we have detected.
	 */
	public static EcoType getType() {

		return Type;
	}

	/**
	 * Are we using any economy system?
	 * 
	 * @return true if we found one.
	 */
	public static boolean isActive() {

		return (Type != EcoType.NONE);
	}

	/**
	 * @return The current economy providers version string
	 */
	public static String getVersion() {

		return version;
	}

	/**
	 * Internal function to set the version string.
	 * 
	 * @param version
	 */
	private static void setVersion(String version) {

		NationsEconomyHandler.version = version;
	}

	/**
	 * Find and configure a suitable economy provider
	 * 
	 * @return true if successful.
	 */
	public static Boolean setupEconomy() {

		/*
		 * Attempt to find Vault for Economy handling
		 */
		try {
			RegisteredServiceProvider<Economy> vaultEcoProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (vaultEcoProvider != null) {
				/*
				 * Flag as using Vault hooks
				 */
				vaultEconomy = vaultEcoProvider.getProvider();
				setVersion(String.format("%s %s", vaultEcoProvider.getProvider().getName(), "via Vault" ));
				Type = EcoType.VAULT;
				return true;
			}
		} catch (NoClassDefFoundError ex) {

		/*
		 * No compatible Economy system found.
		 */
			
		}
		
		return false;
	}

	/**
	 * Check if account exists
	 * 
	 * @param accountName the economy account to check
	 * @return true if the account exists
	 */
	public static boolean hasEconomyAccount(String accountName) {
			return vaultEconomy.hasAccount(accountName);
	}
	
	/**
	 * Check if account exists
	 * 
	 * @param uniqueId the UUID of the account to check
	 * @return true if the account exists
	 */
	public static boolean hasEconomyAccount(UUID uniqueId) {
			return vaultEconomy.hasAccount(Bukkit.getOfflinePlayer(uniqueId));
	}

	/**
	 * Attempt to delete the economy account.
	 * 
	 * @param accountName name of the account to delete
	 */
	public static void removeAccount(String accountName) {

		try { 		// Attempt to zero the account as Vault provides no delete method.
				if (!vaultEconomy.hasAccount(accountName))
					vaultEconomy.createPlayerAccount(accountName);
				
				vaultEconomy.withdrawPlayer(accountName, (vaultEconomy.getBalance(accountName)));

				return;


		} catch (NoClassDefFoundError e) {
		}

		return;
	}

	/**
	 * Returns the accounts current balance
	 * 
	 * @param accountName name of the economy account
	 * @param world name of world to check in (for TNE Reserve)   
	 * @return double containing the total in the account
	 */
	public static double getBalance(String accountName, World world) {

			if (!vaultEconomy.hasAccount(accountName))
				vaultEconomy.createPlayerAccount(accountName);

			return vaultEconomy.getBalance(accountName);
	}

	/**
	 * Returns true if the account has enough money
	 * 
	 * @param accountName name of an economy account
	 * @param amount minimum amount to check against (Double)
	 * @param world name of the world to check in (for TNE Reserve)   
	 * @return true if there is enough in the account
	 */
	public static boolean hasEnough(String accountName, Double amount, World world) {

		if (getBalance(accountName, world) >= amount)
			return true;

		return false;
	}

	/**
	 * Attempts to remove an amount from an account
	 * 
	 * @param accountName name of the account to modify
	 * @param amount amount of currency to remove from the account
	 * @param world name of the world in which to check in (TNE Reserve)   
	 * @return true if successful
	 */
	public static boolean subtract(String accountName, Double amount, World world) {

		Player player = Bukkit.getServer().getPlayer(accountName);
		Transaction transaction = new Transaction(TransactionType.SUBTRACT, player, amount.intValue());
		NationsTransactionEvent event = new NationsTransactionEvent(transaction);
		NationsPreTransactionEvent preEvent = new NationsPreTransactionEvent(transaction);

		BukkitTools.getPluginManager().callEvent(preEvent);

		if (preEvent.isCancelled()) {
			Messages.sendErrorMsg(player, preEvent.getCancelMessage());
			return false;
		}
			if (!vaultEconomy.hasAccount(accountName))
				vaultEconomy.createPlayerAccount(accountName);

			BukkitTools.getPluginManager().callEvent(event);
			return vaultEconomy.withdrawPlayer(accountName, amount).type == EconomyResponse.ResponseType.SUCCESS;
	}

	/**
	 * Add funds to an account.
	 * 
	 * @param accountName account to add funds to
	 * @param amount amount of currency to add
	 * @param world name of world (for TNE Reserve)
	 * @return true if successful
	 */
	public static boolean add(String accountName, Double amount, World world) {

		Player player = Bukkit.getServer().getPlayer(accountName);
		Transaction transaction = new Transaction(TransactionType.ADD, player, amount.intValue());
		NationsTransactionEvent event = new NationsTransactionEvent(transaction);
		NationsPreTransactionEvent preEvent = new NationsPreTransactionEvent(transaction);

		BukkitTools.getPluginManager().callEvent(preEvent);
		
		if (preEvent.isCancelled()) {
			Messages.sendErrorMsg(player, preEvent.getCancelMessage());
			return false;
		}

			if (!vaultEconomy.hasAccount(accountName))
				vaultEconomy.createPlayerAccount(accountName);
			
			Bukkit.getPluginManager().callEvent(event);
			return vaultEconomy.depositPlayer(accountName, amount).type == EconomyResponse.ResponseType.SUCCESS;
	}

	public static boolean setBalance(String accountName, Double amount, World world) {

			if (!vaultEconomy.hasAccount(accountName))
				vaultEconomy.createPlayerAccount(accountName);

			return vaultEconomy.depositPlayer(accountName, (amount - vaultEconomy.getBalance(accountName))).type == EconomyResponse.ResponseType.SUCCESS;
	}

	/**
	 * Format this balance according to the current economy systems settings.
	 * 
	 * @param balance account balance passed by the economy handler
	 * @return string containing the formatted balance
	 */
	public static String getFormattedBalance(double balance) {

		try {
				return vaultEconomy.format(balance);

		} catch (Exception InvalidAPIFunction) {
		}

		return String.format("%.2f", balance);

	}



}