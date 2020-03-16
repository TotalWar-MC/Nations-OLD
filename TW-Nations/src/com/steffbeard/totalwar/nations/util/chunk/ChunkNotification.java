package com.steffbeard.totalwar.nations.util.chunk;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.steffbeard.totalwar.nations.config.ConfigNodes;
import com.steffbeard.totalwar.nations.config.Settings;
import com.steffbeard.totalwar.nations.economy.NationsEconomyHandler;
import com.steffbeard.totalwar.nations.exceptions.NotRegisteredException;
import com.steffbeard.totalwar.nations.objects.NationsWorld;
import com.steffbeard.totalwar.nations.util.BukkitTools;
import com.steffbeard.totalwar.nations.util.CombatUtil;
import com.steffbeard.totalwar.nations.util.coord.WorldCoord;
import com.steffbeard.totalwar.nations.util.file.Colors;
import com.steffbeard.totalwar.nations.util.file.StringMgmt;
import com.steffbeard.totalwar.nations.util.player.PlayerCacheUtil;
import com.steffbeard.totalwar.nations.util.player.PlayerCache.TownBlockStatus;
import com.steffbeard.totalwar.nations.objects.PlotGroup;
import com.steffbeard.totalwar.nations.objects.resident.Resident;
import com.steffbeard.totalwar.nations.objects.town.Town;
import com.steffbeard.totalwar.nations.objects.town.TownBlock;
import com.steffbeard.totalwar.nations.objects.town.TownBlockType;

public class ChunkNotification {

	// Example:
	// ~ Wak Town - Lord Jebus - [Home] [For Sale: 50 Beli] [Shop]

	public static String notificationFormat = Colors.Gold + " ~ %s";
	public static String notificationSpliter = Colors.LightGray + " - ";
	public static String areaWildernessNotificationFormat = Colors.Green + "%s";
	public static String areaWildernessPvPNotificationFormat = Colors.Green + "%s";
	public static String areaTownNotificationFormat = Colors.Green + "%s";
	public static String areaTownPvPNotificationFormat = Colors.Green + "%s";
	public static String ownerNotificationFormat = Colors.LightGreen + "%s";
	public static String noOwnerNotificationFormat = Colors.LightGreen + "%s";
	public static String plotNotficationSplitter = " ";
	public static String plotNotificationFormat = "%s";
	public static String homeBlockNotification = Colors.LightBlue + "[Home]";
	public static String outpostBlockNotification = Colors.LightBlue + "[Outpost]";
	public static String forSaleNotificationFormat = Colors.Yellow + "[For Sale: %s]";
	public static String plotTypeNotificationFormat = Colors.Gold + "[%s]";	
	public static String groupNotificationFormat = Colors.White + "[%s]";

	/**
	 * Called on Config load.
	 * Specifically: Settings.loadCachedLangStrings()
	 */
	public static void loadFormatStrings() {

		notificationFormat = Settings.getConfigLang(ConfigNodes.NOTIFICATION_FORMAT);
		notificationSpliter = Settings.getConfigLang(ConfigNodes.NOTIFICATION_SPLITTER);
		areaWildernessNotificationFormat = Settings.getConfigLang(ConfigNodes.NOTIFICATION_AREA_WILDERNESS);
		areaWildernessPvPNotificationFormat = Settings.getConfigLang(ConfigNodes.NOTIFICATION_AREA_WILDERNESS_PVP);
		areaTownNotificationFormat = Settings.getConfigLang(ConfigNodes.NOTIFICATION_AREA_TOWN);
		areaTownPvPNotificationFormat = Settings.getConfigLang(ConfigNodes.NOTIFICATION_AREA_TOWN_PVP);
		ownerNotificationFormat = Settings.getConfigLang(ConfigNodes.NOTIFICATION_OWNER);
		noOwnerNotificationFormat = Settings.getConfigLang(ConfigNodes.NOTIFICATION_NO_OWNER);
		plotNotficationSplitter = Settings.getConfigLang(ConfigNodes.NOTIFICATION_PLOT_SPLITTER);
		plotNotificationFormat = Settings.getConfigLang(ConfigNodes.NOTIFICATION_PLOT_FORMAT);
		homeBlockNotification = Settings.getConfigLang(ConfigNodes.NOTIFICATION_PLOT_HOMEBLOCK);
		outpostBlockNotification = Settings.getConfigLang(ConfigNodes.NOTIFICATION_PLOT_OUTPOSTBLOCK);
		forSaleNotificationFormat = Settings.getConfigLang(ConfigNodes.NOTIFICATION_PLOT_FORSALE);
		plotTypeNotificationFormat = Settings.getConfigLang(ConfigNodes.NOTIFICATION_PLOT_TYPE);
		groupNotificationFormat = Settings.getConfigLang(ConfigNodes.NOTIFICATION_GROUP);
	}

	WorldCoord from, to;
	boolean fromWild = false, toWild = false, toForSale = false,
			toHomeBlock = false, toOutpostBlock = false, toPlotGroupBlock = false;
	TownBlock fromTownBlock, toTownBlock = null;
	Town fromTown = null, toTown = null;
	Resident fromResident = null, toResident = null;
	TownBlockType fromPlotType = null, toPlotType = null;
	PlotGroup fromPlotGroup = null, toPlotGroup = null;

	public ChunkNotification(WorldCoord from, WorldCoord to) {

		this.from = from;
		this.to = to;

		try {
			fromTownBlock = from.getTownBlock();
			fromPlotType = fromTownBlock.getType();
			try {
				fromTown = fromTownBlock.getTown();
			} catch (NotRegisteredException e) {
			}
			try {
				fromResident = fromTownBlock.getResident();
			} catch (NotRegisteredException e) {
			}
		} catch (NotRegisteredException e) {
			fromWild = true;
		}

		try {
			toTownBlock = to.getTownBlock();
			toPlotType = toTownBlock.getType();
			try {
				toTown = toTownBlock.getTown();
			} catch (NotRegisteredException e) {
			}
			try {
				toResident = toTownBlock.getResident();
			} catch (NotRegisteredException e) {
			}

			toForSale = toTownBlock.getPlotPrice() != -1;
			toHomeBlock = toTownBlock.isHomeBlock();
			toOutpostBlock = toTownBlock.isOutpost();
			toPlotGroupBlock = toTownBlock.hasPlotObjectGroup();

			if (toPlotGroupBlock)
				toForSale = toTownBlock.getPlotObjectGroup().getPrice() != -1;
			
		} catch (NotRegisteredException e) {
			toWild = true;
		}
		
		try {
			if (toTownBlock.hasPlotObjectGroup()) {
				toPlotGroup = toTownBlock.getPlotObjectGroup();
			}
			
			if (fromTownBlock.hasPlotObjectGroup()) {
				fromPlotGroup = fromTownBlock.getPlotObjectGroup();
			}
		} catch (Exception ignored) { }
	}

	public String getNotificationString(Resident resident) {

		if (notificationFormat.length() == 0)
			return null;
		List<String> outputContent = getNotificationContent(resident);
		if (outputContent.size() == 0)
			return null;
		return String.format(notificationFormat, StringMgmt.join(outputContent, notificationSpliter));
	}

	public List<String> getNotificationContent(Resident resident) {

		List<String> out = new ArrayList<String>();
		String output;

		output = getAreaNotification(resident);
		if (output != null && output.length() > 0)
			out.add(output);
		
		// Only adds this if entering the wilderness
		output = getAreaPvPNotification();
		if (output != null && output.length() > 0)
			out.add(output);
		
		// Only show the owner of individual plots if they do not have this mode applied 
		if (!resident.hasMode("ignoreplots")) {
			output = getOwnerNotification();
			if (output != null && output.length() > 0)
				out.add(output);
		}
	
		// Only adds this IF in town.
		output = getTownPVPNotification();
		if (output != null && output.length() > 0)
			out.add(output);

		// Only show the names of plots if they do not have this mode applied
		if (!resident.hasMode("ignoreplots")) {
			output = getPlotNotification();
			if (output != null && output.length() > 0)
				out.add(output);
		}

		return out;
	}

	public String getAreaNotification(Resident resident) {

		if (fromWild ^ toWild || !fromWild && !toWild && fromTown != null && toTown != null && fromTown != toTown) {
			if (toWild) {
				try {
					if (Settings.getNationZonesEnabled() && Settings.getNationZonesShowNotifications()) {
						Player player = BukkitTools.getPlayer(resident.getName());
						NationsWorld toWorld = this.to.getNationsWorld();
						try {
							if (PlayerCacheUtil.getTownBlockStatus(player, this.to).equals(TownBlockStatus.NATION_ZONE)) {
								Town nearestTown = null; 
								nearestTown = toWorld.getClosestTownWithNationFromCoord(this.to.getCoord(), nearestTown);
								return String.format(areaWildernessNotificationFormat, String.format(Settings.getLangString("nation_zone_this_area_under_protection_of"), toWorld.getUnclaimedZoneName(), nearestTown.getNation().getName()));
							}
						} catch (NotRegisteredException ignored) {
						}
					}
					
					return String.format(areaWildernessNotificationFormat, to.getNationsWorld().getUnclaimedZoneName());
				} catch (NotRegisteredException ex) {
					// Not a Nations registered world
				}
			
			} else if (Settings.isNotificationsTownNamesVerbose())
				return String.format(areaTownNotificationFormat, toTown.getFormattedName());
			else 
				return String.format(areaTownNotificationFormat, toTown);
			
		} else if (fromWild && toWild) 
			try {
				if (Settings.getNationZonesEnabled() && Settings.getNationZonesShowNotifications()) {
					Player player = BukkitTools.getPlayer(resident.getName());
					NationsWorld toWorld = this.to.getNationsWorld();
					try {
						if (PlayerCacheUtil.getTownBlockStatus(player, this.to).equals(TownBlockStatus.NATION_ZONE) && PlayerCacheUtil.getTownBlockStatus(player, this.from).equals(TownBlockStatus.UNCLAIMED_ZONE)) {
							Town nearestTown = null; 
							nearestTown = toWorld.getClosestTownWithNationFromCoord(this.to.getCoord(), nearestTown);
							return String.format(areaWildernessNotificationFormat, String.format(Settings.getLangString("nation_zone_this_area_under_protection_of"), toWorld.getUnclaimedZoneName(), nearestTown.getNation().getName()));
						} else if (PlayerCacheUtil.getTownBlockStatus(player, this.to).equals(TownBlockStatus.UNCLAIMED_ZONE) && PlayerCacheUtil.getTownBlockStatus(player, this.from).equals(TownBlockStatus.NATION_ZONE)) {
							return String.format(areaWildernessNotificationFormat, to.getNationsWorld().getUnclaimedZoneName());
						}
					} catch (NotRegisteredException ignored) {
					}
				}
			} catch (NotRegisteredException ignored) {
			}
		return null;
	}
	
	public String getAreaPvPNotification() {

		if (fromWild ^ toWild || !fromWild && !toWild && fromTown != null && toTown != null && fromTown != toTown) {
			if (toWild)
				try {
					return String.format(areaWildernessPvPNotificationFormat, ((to.getNationsWorld().isPVP() && testWorldPVP()) ? Colors.Red + " (PvP)" : ""));
				} catch (NotRegisteredException ex) {
					// Not a Nations registered world
				}
		}
		return null;
	}

	public String getOwnerNotification() {
			
		if (((fromResident != toResident) || ((fromTownBlock != null) && (toTownBlock != null) && (!fromTownBlock.getName().equalsIgnoreCase(toTownBlock.getName()))))
				&& !toWild) {
			
			if (toResident != null)
				if (Settings.isNotificationOwnerShowingNationTitles()) {
					return String.format(ownerNotificationFormat, (toTownBlock.getName().isEmpty()) ? toResident.getFormattedTitleName() : toTownBlock.getName());
				} else {
					return String.format(ownerNotificationFormat, (toTownBlock.getName().isEmpty()) ? toResident.getFormattedName() : toTownBlock.getName());
				}
			else
				return  String.format(noOwnerNotificationFormat, (toTownBlock.getName().isEmpty()) ? Settings.getUnclaimedPlotName() : toTownBlock.getName());

		}
		return null;
	}

	public String getTownPVPNotification() {

		if (!toWild && ((fromWild) || (toTownBlock.getPermissions().pvp != fromTownBlock.getPermissions().pvp))) {
			try {
				return String.format(areaTownPvPNotificationFormat, ( !CombatUtil.preventPvP(to.getNationsWorld(), toTownBlock) ? Colors.Red + "(PvP)" : Colors.Green + "(No PVP)"));
			} catch (NotRegisteredException e) {
				// Not a Nations registered world.
			}
		}
		return null;
	}

	private boolean testWorldPVP() {

		try {
			return Bukkit.getServer().getWorld(to.getNationsWorld().getName()).getPVP();
		} catch (NotRegisteredException e) {
			// Not a Nations registered world
			return true;
		}
	}

	public String getPlotNotification() {

		if (plotNotificationFormat.length() == 0)
			return null;
		List<String> outputContent = getPlotNotificationContent();
		if (outputContent.size() == 0)
			return null;
		return String.format(plotNotificationFormat, StringMgmt.join(outputContent, plotNotficationSplitter));
	}

	public List<String> getPlotNotificationContent() {

		List<String> out = new ArrayList<String>();
		String output;

		output = getHomeblockNotification();
		if (output != null && output.length() > 0)
			out.add(output);

		output = getOutpostblockNotification();
		if (output != null && output.length() > 0)
			out.add(output);

		output = getForSaleNotification();
		if (output != null && output.length() > 0)
			out.add(output);

		output = getPlotTypeNotification();
		if (output != null && output.length() > 0)
			out.add(output);
		
		output = getGroupNotification();
		if (output != null && output.length() > 0)
			out.add(output);

		return out;
	}

	public String getHomeblockNotification() {

		if (toHomeBlock)
			return homeBlockNotification;
		return null;
	}

	public String getOutpostblockNotification() {

		if (toOutpostBlock)
			return outpostBlockNotification;
		return null;
	}

	public String getForSaleNotification() {

		// Were heading to a plot group do some things differently
		if (toForSale && toPlotGroupBlock && (fromPlotGroup != toPlotGroup))
			return String.format(forSaleNotificationFormat, NationsEconomyHandler.getFormattedBalance(toTownBlock.getPlotObjectGroup().getPrice()));
		
		if (toForSale && !toPlotGroupBlock)
			return String.format(forSaleNotificationFormat, NationsEconomyHandler.getFormattedBalance(toTownBlock.getPlotPrice()));
		return null;
	}
	
	public String getGroupNotification() {
		if (toPlotGroupBlock && (fromPlotGroup != toPlotGroup))
			return String.format(groupNotificationFormat, toTownBlock.getPlotObjectGroup().getName());
		return null;
	}

	public String getPlotTypeNotification() {

		if (fromPlotType != toPlotType && toPlotType != null && toPlotType != TownBlockType.RESIDENTIAL)
			return String.format(plotTypeNotificationFormat, toPlotType.toString());
		return null;
	}
}
