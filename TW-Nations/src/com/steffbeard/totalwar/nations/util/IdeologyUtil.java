//package com.steffbeard.totalwar.nations.util;
//
//import java.text.NumberFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Random;
//import java.util.UUID;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import org.bukkit.*;
//import org.bukkit.configuration.ConfigurationSection;
//import org.bukkit.configuration.file.FileConfiguration;
//import org.bukkit.entity.EntityType;
//import org.bukkit.entity.Firework;
//import org.bukkit.entity.Player;
//import org.bukkit.inventory.Inventory;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.FireworkMeta;
//
//import com.steffbeard.totalwar.nations.objects.town.Town;
//
//import net.md_5.bungee.api.chat.TextComponent;
//
//public final class IdeologyUtil {
//
//    private IdeologyUtil() {
//
//    }
//
//    public static void promoteWhoeverHasMostNoise(Town town, boolean save) {
//        int highestScore = 0;
//        UUID newOwner = null;
//        for (UUID uuid : town.getIdiocracyScore().keySet()) {
//            if (town.getIdiocracyScore().get(uuid) > highestScore) {
//                highestScore = town.getIdiocracyScore().get(uuid);
//                newOwner = uuid;
//            }
//        }
//        if (newOwner == null) {
//            return;
//        }
//        HashMap<UUID, String> people = new HashMap<>(town.getRawPeople());
//        for (UUID uuid : people.keySet()) {
//            if (people.get(uuid).contains(Constants.OWNER)) {
//                town.getRawPeople().put(uuid, "member");
//            }
//        }
//        town.getRawPeople().put(newOwner, Constants.OWNER);
//        town.getIdiocracyScore().clear();
//        if (save) {
//            TownManager.getInstance().saveTown(town);
//        }
//    }
//
//    public static void promoteWhoeverHasMostMerit(Town town, boolean save) {
//        UUID lowestOwner = null;
//        int lowestOwnerScore = 99999999;
//        UUID highestMember = null;
//        int highestMemberScore = 0;
//        for (UUID uuid : town.getRawPeople().keySet()) {
//            String role = town.getRawPeople().get(uuid);
//            if (role.contains("member")) {
//                int score = IdeologyUtil.calculateMerit(uuid, town);
//                if (score > highestMemberScore) {
//                    highestMember = uuid;
//                    highestMemberScore = score;
//                }
//            } else if (role.contains(Constants.OWNER)) {
//                int score = IdeologyUtil.calculateMerit(uuid, town);
//                if (score < lowestOwnerScore) {
//                    lowestOwnerScore = score;
//                    lowestOwner = uuid;
//                }
//            }
//        }
//        if (lowestOwner != null && highestMember != null && lowestOwnerScore < highestMemberScore) {
//            town.setPeople(lowestOwner, "member");
//            town.setPeople(highestMember, Constants.OWNER);
//            if (save) {
//                TownManager.getInstance().saveTown(town);
//            }
//        }
//    }
//
//    public static void checkNoise(Town town, Player player) {
//        if (town == null) {
//            return;
//        }
//        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
//        if (government.getGovernmentType() != GovernmentType.IDIOCRACY) {
//            return;
//        }
//        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
//        int score = town.getIdiocracyScore().getOrDefault(civilian.getUuid(), 0);
//        UUID demoteMe = null;
//        for (UUID uuid : town.getRawPeople().keySet()) {
//            if (town.getRawPeople().get(uuid).contains(Constants.OWNER)) {
//                if (town.getIdiocracyScore().getOrDefault(uuid, 0) < score) {
//                    demoteMe = uuid;
//                    break;
//                }
//            }
//        }
//        if (demoteMe != null) {
//            town.setPeople(demoteMe, "member");
//            town.setPeople(player.getUniqueId(), Constants.OWNER);
//            TownManager.getInstance().saveTown(town);
//            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(demoteMe);
//            String name = offlinePlayer.getName() == null ? "???" : offlinePlayer.getName();
//            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
//                    civilian.getLocale(), "merit-new-owner"
//            ).replace("$1", name));
//            spawnRandomFirework(player);
//        }
//    }
//
//    public static void checkMerit(Town town, Player player) {
//        if (town == null) {
//            return;
//        }
//        Government government = GovernmentManager.getInstance().getGovernment(town.getGovernmentType());
//        if (government.getGovernmentType() != GovernmentType.MERITOCRACY) {
//            return;
//        }
//        Civilian civilian = CivilianManager.getInstance().getCivilian(player.getUniqueId());
//        int score = IdeologyUtil.calculateMerit(player.getUniqueId(), town);
//        UUID demoteMe = null;
//        for (UUID uuid : town.getRawPeople().keySet()) {
//            if (town.getRawPeople().get(uuid).contains(Constants.OWNER)) {
//                if (IdeologyUtil.calculateMerit(uuid, town) < score) {
//                    demoteMe = uuid;
//                    break;
//                }
//            }
//        }
//        if (demoteMe != null) {
//            town.setPeople(demoteMe, "member");
//            town.setPeople(player.getUniqueId(), Constants.OWNER);
//            TownManager.getInstance().saveTown(town);
//            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(demoteMe);
//            String name = offlinePlayer.getName() == null ? "???" : offlinePlayer.getName();
//            player.sendMessage(Civs.getPrefix() + LocaleManager.getInstance().getTranslation(
//                    civilian.getLocale(), "merit-new-owner"
//            ).replace("$1", name));
//            spawnRandomFirework(player);
//        }
//    }
//
//    public static int calculateMerit(UUID uuid, Town forTown) {
//        Civilian civilian = CivilianManager.getInstance().getCivilian(uuid);
//        int basePoints = (int) (civilian.getPoints() - (double) civilian.getDeaths() / 4);
//
//        for (Town town : TownManager.getInstance().getTowns()) {
//            if (!town.equals(forTown) || !town.getRawPeople().containsKey(civilian.getUuid())) {
//                continue;
//            }
//            int townPoints = 0;
//            for (Region region : TownManager.getInstance().getContainingRegions(town.getName())) {
//                if (region.getRawPeople().containsKey(civilian.getUuid()) &&
//                        region.getRawPeople().get(civilian.getUuid()).contains(Constants.OWNER)) {
//                    RegionType regionType = (RegionType) ItemManager.getInstance().getItemType(region.getType());
//                    townPoints += 4 * regionType.getLevel();
//                }
//            }
//            return basePoints + townPoints;
//        }
//
//        return 0;
//    }