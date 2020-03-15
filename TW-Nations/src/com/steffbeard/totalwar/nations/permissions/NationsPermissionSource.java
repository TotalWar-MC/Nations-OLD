package com.steffbeard.totalwar.nations.permissions;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import com.steffbeard.totalwar.nations.Main;
import com.steffbeard.totalwar.nations.NationsUniverse;
import com.steffbeard.totalwar.nations.config.Messages;
import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.objects.NationsWorld;
import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.util.BukkitTools;

/**
 * @author ElgarL
 * 
 *         Manager for Permission provider plugins
 * 
 */
public abstract class NationsPermissionSource {

	protected Settings settings;
	protected Main plugin;

	protected GroupManager groupManager = null;

	abstract public String getPrefixSuffix(Resident resident, String node);

	abstract public int getGroupPermissionIntNode(String playerName, String node);
	
	abstract public int getPlayerPermissionIntNode(String playerName, String node);

	//abstract public boolean hasPermission(Player player, String node);

	abstract public String getPlayerGroup(Player player);

	abstract public String getPlayerPermissionStringNode(String playerName, String node);

	protected int getEffectivePermIntNode(String playerName, String node) {

		/*
		 * Bukkit doesn't support non boolean nodes
		 * so treat the same as bPerms
		 */
		Player player = BukkitTools.getPlayer(playerName);

		for (PermissionAttachmentInfo test : player.getEffectivePermissions()) {
			if (test.getPermission().startsWith(node + ".")) {
				String[] split = test.getPermission().split("\\.");
				try {
					return Integer.parseInt(split[split.length - 1]);
				} catch (NumberFormatException e) {
				}
			}
		}

		return -1;

	}

	/**
	 * Test if the player has a wild override to permit this action.
	 *  
	 * @param world - NationsWorld object.
	 * @param player - Player.
	 * @param material - Material being tested.
	 * @param action - Action type.
	 * @return true if the action is permitted.
	 */
	public boolean hasWildOverride(NationsWorld world, Player player, Material material, Permission.ActionType action) {

		// check for permissions

		String blockPerm = PermissionNodes.TOWNY_WILD_ALL.getNode(action.toString().toLowerCase() + "." + material);

		boolean hasBlock = has(player, blockPerm);

		/*
		 * If the player has the data node permission registered directly
		 *  or
		 * the player has the block permission and the data node isn't registered
		 */
		if (hasBlock)
			return true;

		// No node set but we are using permissions so check world settings
		// (without UnclaimedIgnoreId's).
		switch (action) {

		case BUILD:
			return world.getUnclaimedZoneBuild();
		case DESTROY:
			return world.getUnclaimedZoneDestroy();
		case SWITCH:
			return world.getUnclaimedZoneSwitch();
		case ITEM_USE:
			return world.getUnclaimedZoneItemUse();
		}

		return false;
	}

	public boolean unclaimedZoneAction(NationsWorld world, Material material, Permission.ActionType action) {
		
		switch (action) {

			case BUILD:
				return world.getUnclaimedZoneBuild() || world.isUnclaimedZoneIgnoreMaterial(material);
			case DESTROY:
				return world.getUnclaimedZoneDestroy() || world.isUnclaimedZoneIgnoreMaterial(material);
			case SWITCH:
				return world.getUnclaimedZoneSwitch() || world.isUnclaimedZoneIgnoreMaterial(material);
			case ITEM_USE:
				return world.getUnclaimedZoneItemUse() || world.isUnclaimedZoneIgnoreMaterial(material);
		}

		return false;
	}

	/**
	 * Test if the player has an own town (or all town) override to permit this action.
	 * 
	 * @param player - Player.
	 * @param material - Material being tested.
	 * @param action - ActionType.
	 * @return true if the action is permitted.
	 */
	public boolean hasOwnTownOverride(Player player, Material material, Permission.ActionType action) {

		//check for permissions
		String blockPerm = PermissionNodes.TOWNY_CLAIMED_ALL.getNode("owntown." + action.toString().toLowerCase() + "." + material);

		boolean hasBlock = has(player, blockPerm);

		Messages.sendDebugMsg(player.getName() + " - owntown (Block: " + material);

		/*
		 * If the player has the data node permission registered directly
		 *  or
		 * the player has the block permission and the data node isn't registered
		 *  or
		 * the player has an All town Override
		 */
		if (hasBlock || hasAllTownOverride(player, material, action))
			return true;


		return false;
	}

	/**
	 * Test if the player has a 'town owned', 'Own town' or 'all town' override to permit this action.
	 * 
	 * @param player - Player.
	 * @param material - Material being tested.
	 * @param action - ActionType.
	 * @return - True if action is permitted.
	 */
	public boolean hasTownOwnedOverride(Player player, Material material, Permission.ActionType action) {

		//check for permissions
		String blockPerm = PermissionNodes.TOWNY_CLAIMED_ALL.getNode("townowned." + action.toString().toLowerCase() + "." + material);

		boolean hasBlock = has(player, blockPerm);

		Messages.sendDebugMsg(player.getName() + " - townowned (Block: " + hasBlock);

		/*
		 * If the player has the data node permission registered directly
		 *  or
		 * the player has the block permission and the data node isn't registered
		 *  or
		 * the player has an Own Town Override
		 *  or
		 * the player has an All town Override
		 */
		if (hasBlock || hasOwnTownOverride(player, material, action) || hasAllTownOverride(player, material, action))
			return true;

		return false;
	}

	/**
	 * Test if the player has an all town override to permit this action.
	 * 
	 * @param player - Player.
	 * @param material - Material being tested.
	 * @param action - ActionType.
	 * @return true if the action is permitted.
	 */
	public boolean hasAllTownOverride(Player player, Material material, Permission.ActionType action) {

		//check for permissions
		String blockPerm = PermissionNodes.TOWNY_CLAIMED_ALL.getNode("alltown." + action.toString().toLowerCase() + "." + material);

		boolean hasBlock = has(player, blockPerm);

		Messages.sendDebugMsg(player.getName() + " - alltown (Block: " + hasBlock);

		/*
		 * If the player has the data node permission registered directly
		 *  or
		 * the player has the block permission and the data node isn't registered
		 */
		if (hasBlock)
			return true;

		return false;
	}	

	public boolean isNationsAdmin(Player player) {

		return ((player == null) || player.isOp()) || has(player, PermissionNodes.TOWNY_ADMIN.getNode());

	}

	public boolean testPermission(Player player, String perm) {
		return NationsUniverse.getInstance().getPermissionSource().isNationsAdmin(player) || (has(player, perm));
	}

	/**
	 * All permission checks should go through here.
	 * 
	 * Returns true if a player has a certain permission node.
	 * 
	 * @param player - Player to check
	 * @param node - Permission node to check for
	 * @return true if the player has this permission node.
	 */
	public boolean has(Player player, String node) {

		if (player.isOp())
			return true;

		/*
		 * Node has been set or negated so return the actual value
		 */
		if (player.isPermissionSet(node))
			return player.hasPermission(node);

		/*
		 * Check for a parent with a wildcard
		 */
		final String[] parts = node.split("\\.");
		final StringBuilder builder = new StringBuilder(node.length());
		for (String part : parts) {
			builder.append('*');
			if (player.hasPermission("-" + builder.toString())) {
				return false;
			}
			if (player.hasPermission(builder.toString())) {
				return true;
			}
			builder.deleteCharAt(builder.length() - 1);
			builder.append(part).append('.');
		}

		/*
		 * No parent found so we don't have this node.
		 */
		return false;

	}
}
