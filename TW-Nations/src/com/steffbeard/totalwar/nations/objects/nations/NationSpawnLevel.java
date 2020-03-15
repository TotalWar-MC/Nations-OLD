package com.steffbeard.totalwar.nations.objects.nations;

import org.bukkit.entity.Player;

import com.steffbeard.totalwar.nations.Main;
import com.steffbeard.totalwar.nations.NationsAPI;
import com.steffbeard.totalwar.nations.NationsUniverse;
import com.steffbeard.totalwar.nations.config.ConfigNodes;
import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.exceptions.NationsException;
import com.steffbeard.totalwar.nations.permissions.PermissionNodes;

public enum NationSpawnLevel {
	
	PART_OF_NATION(
			ConfigNodes.GNATION_SETTINGS_ALLOW_NATION_SPAWN,
			"msg_err_nation_spawn_forbidden",
			"msg_err_nation_spawn_forbidden_war",
			"msg_err_nation_spawn_forbidden_peace",
			ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL,
			PermissionNodes.TOWNY_NATION_SPAWN_NATION.getNode()),	
	NATION_ALLY(
			ConfigNodes.GNATION_SETTINGS_ALLOW_NATION_SPAWN_TRAVEL_ALLY,
			"msg_err_nation_spawn_ally_forbidden",
			"msg_err_nation_spawn_nation_forbidden_war",
			"msg_err_nation_spawn_nation_forbidden_peace",
			ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL_ALLY,
			PermissionNodes.TOWNY_NATION_SPAWN_ALLY.getNode()),
	UNAFFILIATED(
			ConfigNodes.GNATION_SETTINGS_ALLOW_NATION_SPAWN_TRAVEL,
			"msg_err_public_nation_spawn_forbidden",
			"msg_err_public_nation_spawn_forbidden_war",
			"msg_err_public_nation_spawn_forbidden_peace",
			ConfigNodes.ECO_PRICE_TOWN_SPAWN_TRAVEL_PUBLIC,
			PermissionNodes.TOWNY_SPAWN_PUBLIC.getNode()),
	ADMIN(
			null,
			null,
			null,
			null,
			null,
			null);

	private ConfigNodes isAllowingConfigNode, ecoPriceConfigNode;
	private String permissionNode, notAllowedLangNode, notAllowedLangNodeWar, notAllowedLangNodePeace;

	private NationSpawnLevel(ConfigNodes isAllowingConfigNode, String notAllowedLangNode, String notAllowedLangNodeWar, String notAllowedLangNodePeace, ConfigNodes ecoPriceConfigNode, String permissionNode) {

		this.isAllowingConfigNode = isAllowingConfigNode;
		this.notAllowedLangNode = notAllowedLangNode;
		this.notAllowedLangNodeWar = notAllowedLangNodeWar;
		this.notAllowedLangNodePeace = notAllowedLangNodePeace;
		this.ecoPriceConfigNode = ecoPriceConfigNode;
		this.permissionNode = permissionNode;
	}

	public void checkIfAllowed(Main plugin, Player player, Nation nation) throws NationsException {

		if (!(isAllowed(nation) && hasPermissionNode(plugin, player, nation))) {
			boolean war = NationsAPI.getInstance().isWarTime();
			NSpawnLevel level = Settings.getNSpawnLevel(this.isAllowingConfigNode);
			if(level == NSpawnLevel.WAR && !war) {
				throw new NationsException(Settings.getLangString(notAllowedLangNodeWar));
			}
			else if(level == NSpawnLevel.PEACE && war) {
				throw new NationsException(Settings.getLangString(notAllowedLangNodePeace));
			}
			throw new NationsException(Settings.getLangString(notAllowedLangNode));
		}
	}

	public boolean isAllowed(Nation nation) {
		return this == NationSpawnLevel.ADMIN || isAllowedNation(nation);
	}

	public boolean hasPermissionNode(Main plugin, Player player, Nation nation) {

		return this == NationSpawnLevel.ADMIN || (NationsUniverse.getInstance().getPermissionSource().has(player, this.permissionNode)) && (isAllowedNation(nation));
	}
	
	private boolean isAllowedNation(Nation nation) {
		boolean war = NationsAPI.getInstance().isWarTime();
		NSpawnLevel level = Settings.getNSpawnLevel(this.isAllowingConfigNode);
		return level == NSpawnLevel.TRUE || (level != NSpawnLevel.FALSE && ((level == NSpawnLevel.WAR) == war));
	}

	public double getCost() {

		return this == NationSpawnLevel.ADMIN ? 0 : Settings.getDouble(ecoPriceConfigNode);
	}

	public double getCost(Nation nation) {

		return this == NationSpawnLevel.ADMIN ? 0 : nation.getSpawnCost();
	}
	
	public enum NSpawnLevel {
		TRUE,
		FALSE,
		WAR,
		PEACE
	}
}
