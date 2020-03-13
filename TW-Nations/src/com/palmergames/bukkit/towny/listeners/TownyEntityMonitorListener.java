// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.listeners;

import java.util.Iterator;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Coord;
import org.bukkit.Location;
import com.palmergames.bukkit.towny.object.EconomyHandler;
import com.palmergames.bukkit.towny.permissions.PermissionNodes;
import com.palmergames.bukkit.towny.exceptions.EconomyException;
import com.palmergames.bukkit.towny.TownyEconomyHandler;
import com.palmergames.bukkit.towny.object.EconomyAccount;
import com.palmergames.bukkit.towny.war.eventwar.WarSpoils;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.war.eventwar.War;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.entity.Entity;
import com.palmergames.bukkit.towny.war.siegewar.SiegeWarDeathController;
import com.palmergames.bukkit.towny.TownySettings;
import org.bukkit.entity.Projectile;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.event.entity.EntityDeathEvent;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.event.Listener;

public class TownyEntityMonitorListener implements Listener
{
    private final Towny plugin;
    
    public TownyEntityMonitorListener(final Towny instance) {
        this.plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(final EntityDeathEvent event) throws NotRegisteredException {
        final Entity defenderEntity = (Entity)event.getEntity();
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (!TownyAPI.getInstance().isTownyWorld(event.getEntity().getWorld())) {
            return;
        }
        if (defenderEntity instanceof Player) {
            if (defenderEntity.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
                final EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent)defenderEntity.getLastDamageCause();
                final Entity attackerEntity = damageEvent.getDamager();
                final Player defenderPlayer = (Player)defenderEntity;
                Player attackerPlayer = null;
                Resident attackerResident = null;
                Resident defenderResident = null;
                try {
                    defenderResident = townyUniverse.getDataSource().getResident(defenderPlayer.getName());
                }
                catch (NotRegisteredException e) {
                    return;
                }
                if (attackerEntity instanceof Projectile) {
                    final Projectile projectile = (Projectile)attackerEntity;
                    if (projectile.getShooter() instanceof Player) {
                        attackerPlayer = (Player)projectile.getShooter();
                        try {
                            attackerResident = townyUniverse.getDataSource().getResident(attackerPlayer.getName());
                        }
                        catch (NotRegisteredException ex) {}
                    }
                }
                else if (attackerEntity instanceof Player) {
                    attackerPlayer = (Player)attackerEntity;
                    try {
                        attackerResident = townyUniverse.getDataSource().getResident(attackerPlayer.getName());
                    }
                    catch (NotRegisteredException ex2) {}
                }
                if (TownySettings.getWarSiegeEnabled() && attackerResident != null && defenderResident != null) {
                    SiegeWarDeathController.evaluateSiegePvPDeath(defenderPlayer, attackerPlayer, defenderResident, attackerResident);
                }
                this.deathPayment(attackerPlayer, defenderPlayer, attackerResident, defenderResident);
                if (attackerPlayer instanceof Player) {
                    this.isJailingAttackers(attackerPlayer, defenderPlayer, attackerResident, defenderResident);
                }
                if (TownyAPI.getInstance().isWarTime()) {
                    this.wartimeDeathPoints(attackerPlayer, defenderPlayer, attackerResident, defenderResident);
                }
            }
            else if (!TownySettings.isDeathPricePVPOnly() && TownySettings.isChargingDeath()) {
                final Player defenderPlayer2 = (Player)defenderEntity;
                Resident defenderResident2 = null;
                try {
                    defenderResident2 = townyUniverse.getDataSource().getResident(defenderPlayer2.getName());
                }
                catch (NotRegisteredException e2) {
                    return;
                }
                this.deathPayment(defenderPlayer2, defenderResident2);
            }
        }
    }
    
    private void wartimeDeathPoints(final Player attackerPlayer, final Player defenderPlayer, final Resident attackerResident, final Resident defenderResident) {
        if (attackerPlayer != null && defenderPlayer != null && TownyAPI.getInstance().isWarTime()) {
            try {
                if (CombatUtil.isAlly(attackerPlayer.getName(), defenderPlayer.getName())) {
                    return;
                }
                if (attackerResident.hasTown() && War.isWarringTown(attackerResident.getTown()) && defenderResident.hasTown() && War.isWarringTown(defenderResident.getTown())) {
                    if (TownySettings.isRemovingOnMonarchDeath()) {
                        this.monarchDeath(attackerPlayer, defenderPlayer, attackerResident, defenderResident);
                    }
                    if (TownySettings.getWarPointsForKill() > 0) {
                        TownyUniverse.getInstance().getWarEvent().townScored(defenderResident.getTown(), attackerResident.getTown(), defenderPlayer, attackerPlayer, TownySettings.getWarPointsForKill());
                    }
                }
            }
            catch (NotRegisteredException ex) {}
        }
    }
    
    private void monarchDeath(final Player attackerPlayer, final Player defenderPlayer, final Resident attackerResident, final Resident defenderResident) {
        final War warEvent = TownyUniverse.getInstance().getWarEvent();
        try {
            final Nation defenderNation = defenderResident.getTown().getNation();
            final Town defenderTown = defenderResident.getTown();
            if (warEvent.isWarringNation(defenderNation) && defenderResident.isKing()) {
                TownyMessaging.sendGlobalMessage(TownySettings.getWarTimeKingKilled(defenderNation));
                if (attackerResident != null) {
                    warEvent.remove(attackerResident.getTown(), defenderNation);
                }
            }
            else if (warEvent.isWarringNation(defenderNation) && defenderResident.isMayor()) {
                TownyMessaging.sendGlobalMessage(TownySettings.getWarTimeMayorKilled(defenderTown));
                if (attackerResident != null) {
                    warEvent.remove(attackerResident.getTown(), defenderResident.getTown());
                }
            }
        }
        catch (NotRegisteredException ex) {}
    }
    
    public void deathPayment(final Player defenderPlayer, final Resident defenderResident) throws NotRegisteredException {
        if (TownyAPI.getInstance().getTownBlock(defenderPlayer.getLocation()) != null && (TownyAPI.getInstance().getTownBlock(defenderPlayer.getLocation()).getType() == TownBlockType.ARENA || TownyAPI.getInstance().getTownBlock(defenderPlayer.getLocation()).getType() == TownBlockType.JAIL)) {
            return;
        }
        if (defenderResident.isJailed()) {
            return;
        }
        double total = 0.0;
        try {
            if (TownySettings.getDeathPrice() > 0.0) {
                double price = TownySettings.getDeathPrice();
                if (!TownySettings.isDeathPriceType()) {
                    price *= defenderResident.getAccount().getHoldingBalance();
                    if (TownySettings.isDeathPricePercentageCapped() && price > TownySettings.getDeathPricePercentageCap()) {
                        price = TownySettings.getDeathPricePercentageCap();
                    }
                }
                if (!defenderResident.getAccount().canPayFromHoldings(price)) {
                    price = defenderResident.getAccount().getHoldingBalance();
                }
                if (!TownySettings.isEcoClosedEconomyEnabled()) {
                    defenderResident.getAccount().payTo(price, new WarSpoils(), "Death Payment");
                }
                else {
                    defenderResident.getAccount().pay(price, "Death Payment");
                }
                total += price;
                TownyMessaging.sendMsg(defenderPlayer, String.format(TownySettings.getLangString("msg_you_lost_money_dying"), TownyEconomyHandler.getFormattedBalance(price)));
            }
        }
        catch (EconomyException e) {
            TownyMessaging.sendErrorMsg(defenderPlayer, TownySettings.getLangString("msg_err_could_not_take_deathfunds"));
        }
        try {
            if (TownySettings.getDeathPriceTown() > 0.0) {
                double price = TownySettings.getDeathPriceTown();
                if (!TownySettings.isDeathPriceType()) {
                    price *= defenderResident.getTown().getAccount().getHoldingBalance();
                }
                if (!defenderResident.getTown().getAccount().canPayFromHoldings(price)) {
                    price = defenderResident.getTown().getAccount().getHoldingBalance();
                }
                if (!TownySettings.isEcoClosedEconomyEnabled()) {
                    defenderResident.getTown().getAccount().payTo(price, new WarSpoils(), "Death Payment Town");
                }
                else {
                    defenderResident.getTown().getAccount().pay(price, "Death Payment Town");
                }
                total += price;
                TownyMessaging.sendTownMessagePrefixed(defenderResident.getTown(), String.format(TownySettings.getLangString("msg_your_town_lost_money_dying"), TownyEconomyHandler.getFormattedBalance(price)));
            }
        }
        catch (EconomyException e) {
            TownyMessaging.sendErrorMsg(defenderPlayer, TownySettings.getLangString("msg_err_couldnt_take_deathfunds"));
        }
        catch (NotRegisteredException e2) {
            TownyMessaging.sendErrorMsg(defenderPlayer, TownySettings.getLangString("msg_err_couldnt_take_town_deathfunds"));
        }
        try {
            if (TownySettings.getDeathPriceNation() > 0.0) {
                double price = TownySettings.getDeathPriceNation();
                if (!TownySettings.isDeathPriceType()) {
                    price *= defenderResident.getTown().getNation().getAccount().getHoldingBalance();
                }
                if (!defenderResident.getTown().getNation().getAccount().canPayFromHoldings(price)) {
                    price = defenderResident.getTown().getNation().getAccount().getHoldingBalance();
                }
                if (!TownySettings.isEcoClosedEconomyEnabled()) {
                    defenderResident.getTown().getNation().getAccount().payTo(price, new WarSpoils(), "Death Payment Nation");
                }
                else {
                    defenderResident.getTown().getNation().getAccount().pay(price, "Death Payment Nation");
                }
                total += price;
                TownyMessaging.sendNationMessagePrefixed(defenderResident.getTown().getNation(), String.format(TownySettings.getLangString("msg_your_nation_lost_money_dying"), TownyEconomyHandler.getFormattedBalance(price)));
            }
        }
        catch (EconomyException e) {
            TownyMessaging.sendErrorMsg(defenderPlayer, TownySettings.getLangString("msg_err_couldnt_take_deathfunds"));
        }
        catch (NotRegisteredException e2) {
            TownyMessaging.sendErrorMsg(defenderPlayer, TownySettings.getLangString("msg_err_couldnt_take_nation_deathfunds"));
        }
    }
    
    public void deathPayment(final Player attackerPlayer, final Player defenderPlayer, final Resident attackerResident, final Resident defenderResident) throws NotRegisteredException {
        if (defenderPlayer != null && TownyUniverse.getInstance().getPermissionSource().testPermission(defenderPlayer, PermissionNodes.TOWNY_BYPASS_DEATH_COSTS.getNode())) {
            return;
        }
        if (attackerPlayer != null && TownyAPI.getInstance().isWarTime() && TownySettings.getWartimeDeathPrice() > 0.0) {
            try {
                if (attackerResident == null) {
                    throw new NotRegisteredException(String.format("The attackingResident %s has not been registered.", attackerPlayer.getName()));
                }
                double price = TownySettings.getWartimeDeathPrice();
                double townPrice = 0.0;
                if (!defenderResident.getAccount().canPayFromHoldings(price)) {
                    townPrice = price - defenderResident.getAccount().getHoldingBalance();
                    price = defenderResident.getAccount().getHoldingBalance();
                }
                if (price > 0.0) {
                    if (!TownySettings.isEcoClosedEconomyEnabled()) {
                        defenderResident.getAccount().payTo(price, attackerResident, "Death Payment (War)");
                        TownyMessaging.sendMsg(attackerPlayer, String.format(TownySettings.getLangString("msg_you_robbed_player"), defenderResident.getName(), TownyEconomyHandler.getFormattedBalance(price)));
                        TownyMessaging.sendMsg(defenderPlayer, String.format(TownySettings.getLangString("msg_player_robbed_you"), attackerResident.getName(), TownyEconomyHandler.getFormattedBalance(price)));
                    }
                    else {
                        defenderResident.getAccount().pay(price, "Death Payment (War)");
                        TownyMessaging.sendMsg(defenderPlayer, String.format(TownySettings.getLangString("msg_you_lost_money"), TownyEconomyHandler.getFormattedBalance(price)));
                    }
                }
                if (townPrice > 0.0) {
                    final Town town = defenderResident.getTown();
                    if (!town.getAccount().canPayFromHoldings(townPrice)) {
                        townPrice = town.getAccount().getHoldingBalance();
                        try {
                            TownyUniverse.getInstance().getWarEvent().remove(attackerResident.getTown(), town);
                        }
                        catch (NotRegisteredException e) {
                            TownyUniverse.getInstance().getWarEvent().remove(town);
                        }
                    }
                    else if (!TownySettings.isEcoClosedEconomyEnabled()) {
                        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_player_couldnt_pay_player_town_bank_paying_instead"), defenderResident.getName(), attackerResident.getName(), townPrice));
                        town.getAccount().payTo(townPrice, attackerResident, String.format("Death Payment (War) (%s couldn't pay)", defenderResident.getName()));
                    }
                    else {
                        TownyMessaging.sendPrefixedTownMessage(town, String.format(TownySettings.getLangString("msg_player_couldnt_pay_player_town_bank_paying_instead"), defenderResident.getName(), attackerResident.getName(), townPrice));
                        town.getAccount().pay(townPrice, String.format("Death Payment (War) (%s couldn't pay)", defenderResident.getName()));
                    }
                }
            }
            catch (NotRegisteredException ex) {}
            catch (EconomyException e2) {
                TownyMessaging.sendErrorMsg(attackerPlayer, TownySettings.getLangString("msg_err_wartime_could_not_take_deathfunds"));
                TownyMessaging.sendErrorMsg(defenderPlayer, TownySettings.getLangString("msg_err_wartime_could_not_take_deathfunds"));
            }
        }
        else if (TownySettings.isChargingDeath() && attackerPlayer != null) {
            if (TownyAPI.getInstance().getTownBlock(defenderPlayer.getLocation()) != null && (TownyAPI.getInstance().getTownBlock(defenderPlayer.getLocation()).getType() == TownBlockType.ARENA || TownyAPI.getInstance().getTownBlock(defenderPlayer.getLocation()).getType() == TownBlockType.JAIL)) {
                return;
            }
            if (defenderResident.isJailed()) {
                return;
            }
            double total = 0.0;
            try {
                if (TownySettings.getDeathPrice() > 0.0) {
                    double price2 = TownySettings.getDeathPrice();
                    if (!TownySettings.isDeathPriceType()) {
                        price2 *= defenderResident.getAccount().getHoldingBalance();
                        System.out.println("percentage death");
                        if (TownySettings.isDeathPricePercentageCapped() && price2 > TownySettings.getDeathPricePercentageCap()) {
                            price2 = TownySettings.getDeathPricePercentageCap();
                        }
                    }
                    if (!defenderResident.getAccount().canPayFromHoldings(price2)) {
                        price2 = defenderResident.getAccount().getHoldingBalance();
                    }
                    if (attackerResident == null) {
                        if (!TownySettings.isEcoClosedEconomyEnabled()) {
                            defenderResident.getAccount().payTo(price2, new WarSpoils(), "Death Payment");
                        }
                        else {
                            defenderResident.getAccount().pay(price2, "Death Payment");
                        }
                    }
                    else if (!TownySettings.isEcoClosedEconomyEnabled()) {
                        defenderResident.getAccount().payTo(price2, attackerResident, "Death Payment");
                    }
                    else {
                        defenderResident.getAccount().pay(price2, "Death Payment");
                    }
                    total += price2;
                    TownyMessaging.sendMsg(defenderPlayer, String.format(TownySettings.getLangString("msg_you_lost_money_dying"), TownyEconomyHandler.getFormattedBalance(price2)));
                }
            }
            catch (EconomyException e3) {
                TownyMessaging.sendErrorMsg(defenderPlayer, TownySettings.getLangString("msg_err_could_not_take_deathfunds"));
            }
            try {
                if (TownySettings.getDeathPriceTown() > 0.0) {
                    double price2 = TownySettings.getDeathPriceTown();
                    if (!TownySettings.isDeathPriceType()) {
                        price2 *= defenderResident.getTown().getAccount().getHoldingBalance();
                    }
                    if (!defenderResident.getTown().getAccount().canPayFromHoldings(price2)) {
                        price2 = defenderResident.getTown().getAccount().getHoldingBalance();
                    }
                    if (attackerResident == null) {
                        if (!TownySettings.isEcoClosedEconomyEnabled()) {
                            defenderResident.getTown().getAccount().payTo(price2, new WarSpoils(), "Death Payment Town");
                        }
                        else {
                            defenderResident.getTown().getAccount().pay(price2, "Death Payment Town");
                        }
                    }
                    else if (!TownySettings.isEcoClosedEconomyEnabled()) {
                        defenderResident.getTown().getAccount().payTo(price2, attackerResident, "Death Payment Town");
                    }
                    else {
                        defenderResident.getTown().getAccount().pay(price2, "Death Payment Town");
                    }
                    total += price2;
                    TownyMessaging.sendTownMessagePrefixed(defenderResident.getTown(), String.format(TownySettings.getLangString("msg_your_town_lost_money_dying"), TownyEconomyHandler.getFormattedBalance(price2)));
                }
            }
            catch (EconomyException e3) {
                TownyMessaging.sendErrorMsg(defenderPlayer, TownySettings.getLangString("msg_err_couldnt_take_deathfunds"));
            }
            catch (NotRegisteredException e4) {
                TownyMessaging.sendErrorMsg(defenderPlayer, TownySettings.getLangString("msg_err_couldnt_take_town_deathfunds"));
            }
            try {
                if (TownySettings.getDeathPriceNation() > 0.0) {
                    double price2 = TownySettings.getDeathPriceNation();
                    if (!TownySettings.isDeathPriceType()) {
                        price2 *= defenderResident.getTown().getNation().getAccount().getHoldingBalance();
                    }
                    if (!defenderResident.getTown().getNation().getAccount().canPayFromHoldings(price2)) {
                        price2 = defenderResident.getTown().getNation().getAccount().getHoldingBalance();
                    }
                    if (attackerResident == null) {
                        if (!TownySettings.isEcoClosedEconomyEnabled()) {
                            defenderResident.getTown().getNation().getAccount().payTo(price2, new WarSpoils(), "Death Payment Nation");
                        }
                        else {
                            defenderResident.getTown().getNation().getAccount().pay(price2, "Death Payment Nation");
                        }
                    }
                    else if (!TownySettings.isEcoClosedEconomyEnabled()) {
                        defenderResident.getTown().getNation().getAccount().payTo(price2, attackerResident, "Death Payment Nation");
                    }
                    else {
                        defenderResident.getTown().getNation().getAccount().pay(price2, "Death Payment Nation");
                    }
                    total += price2;
                    TownyMessaging.sendNationMessagePrefixed(defenderResident.getTown().getNation(), String.format(TownySettings.getLangString("msg_your_nation_lost_money_dying"), TownyEconomyHandler.getFormattedBalance(price2)));
                }
            }
            catch (EconomyException e3) {
                TownyMessaging.sendErrorMsg(defenderPlayer, TownySettings.getLangString("msg_err_couldnt_take_deathfunds"));
            }
            catch (NotRegisteredException e4) {
                TownyMessaging.sendErrorMsg(defenderPlayer, TownySettings.getLangString("msg_err_couldnt_take_nation_deathfunds"));
            }
            if (attackerResident != null && !TownySettings.isEcoClosedEconomyEnabled()) {
                TownyMessaging.sendMsg(attackerResident, String.format(TownySettings.getLangString("msg_you_gained_money_for_killing"), TownyEconomyHandler.getFormattedBalance(total), defenderPlayer.getName()));
            }
        }
    }
    
    public void isJailingAttackers(final Player attackerPlayer, final Player defenderPlayer, final Resident attackerResident, final Resident defenderResident) throws NotRegisteredException {
        if (TownySettings.isJailingAttackingEnemies() || TownySettings.isJailingAttackingOutlaws()) {
            final Location loc = defenderPlayer.getLocation();
            final TownyUniverse townyUniverse = TownyUniverse.getInstance();
            if (!TownyAPI.getInstance().isTownyWorld(defenderPlayer.getLocation().getWorld())) {
                return;
            }
            if (TownyAPI.getInstance().getTownBlock(defenderPlayer.getLocation()) == null) {
                return;
            }
            if (TownyAPI.getInstance().getTownBlock(defenderPlayer.getLocation()).getType() == TownBlockType.ARENA) {
                return;
            }
            if (defenderResident.isJailed()) {
                if (TownyAPI.getInstance().getTownBlock(defenderPlayer.getLocation()).getType() != TownBlockType.JAIL) {
                    TownyMessaging.sendGlobalMessage(String.format(TownySettings.getLangString("msg_killed_attempting_to_escape_jail"), defenderPlayer.getName()));
                }
            }
            else {
                if (!attackerResident.hasTown()) {
                    return;
                }
                if (TownySettings.isJailingAttackingOutlaws()) {
                    Town attackerTown = null;
                    try {
                        attackerTown = attackerResident.getTown();
                    }
                    catch (NotRegisteredException ex) {}
                    if (attackerTown.hasOutlaw(defenderResident)) {
                        if (TownyAPI.getInstance().getTownBlock(loc) == null) {
                            return;
                        }
                        try {
                            if (TownyAPI.getInstance().getTownBlock(loc).getTown().getName() != attackerResident.getTown().getName()) {
                                return;
                            }
                        }
                        catch (NotRegisteredException e1) {
                            e1.printStackTrace();
                        }
                        if (!attackerTown.hasJailSpawn()) {
                            return;
                        }
                        if (TownyAPI.getInstance().isWarTime()) {
                            TownBlock jailBlock = null;
                            Integer index = 1;
                            for (final Location jailSpawn : attackerTown.getAllJailSpawns()) {
                                try {
                                    jailBlock = townyUniverse.getDataSource().getWorld(loc.getWorld().getName()).getTownBlock(Coord.parseCoord(jailSpawn));
                                }
                                catch (TownyException e2) {
                                    e2.printStackTrace();
                                }
                                if (War.isWarZone(jailBlock.getWorldCoord())) {
                                    defenderResident.setJailed(defenderResident, index, attackerTown);
                                    try {
                                        TownyMessaging.sendTitleMessageToResident(defenderResident, "You have been jailed", "Run to the wilderness or wait for a jailbreak.");
                                    }
                                    catch (TownyException ex2) {}
                                    return;
                                }
                                ++index;
                                TownyMessaging.sendDebugMsg("A jail spawn was skipped because the plot has fallen in war.");
                            }
                            TownyMessaging.sendPrefixedTownMessage(attackerTown, TownySettings.getWarPlayerCannotBeJailedPlotFallenMsg());
                            return;
                        }
                        if (!townyUniverse.getPermissionSource().testPermission(attackerPlayer, PermissionNodes.TOWNY_OUTLAW_JAILER.getNode())) {
                            return;
                        }
                        defenderResident.setJailed(defenderResident, 1, attackerTown);
                        return;
                    }
                }
                Town town = null;
                try {
                    town = attackerResident.getTown();
                }
                catch (NotRegisteredException e1) {
                    e1.printStackTrace();
                }
                if (TownyAPI.getInstance().getTownBlock(loc) == null) {
                    return;
                }
                try {
                    if (TownyAPI.getInstance().getTownBlock(loc).getTown().getName() != attackerResident.getTown().getName()) {
                        return;
                    }
                }
                catch (NotRegisteredException e1) {
                    e1.printStackTrace();
                }
                if (!attackerResident.hasNation() || !defenderResident.hasNation()) {
                    return;
                }
                try {
                    if (!attackerResident.getTown().getNation().getEnemies().contains(defenderResident.getTown().getNation())) {
                        return;
                    }
                }
                catch (NotRegisteredException e3) {
                    e3.printStackTrace();
                }
                if (!town.hasJailSpawn()) {
                    return;
                }
                if (TownyAPI.getInstance().isWarTime()) {
                    TownBlock jailBlock = null;
                    Integer index = 1;
                    for (final Location jailSpawn : town.getAllJailSpawns()) {
                        try {
                            jailBlock = townyUniverse.getDataSource().getWorld(loc.getWorld().getName()).getTownBlock(Coord.parseCoord(jailSpawn));
                        }
                        catch (TownyException e2) {
                            e2.printStackTrace();
                        }
                        if (War.isWarZone(jailBlock.getWorldCoord())) {
                            defenderResident.setJailed(defenderResident, index, town);
                            try {
                                TownyMessaging.sendTitleMessageToResident(defenderResident, "You have been jailed", "Run to the wilderness or wait for a jailbreak.");
                            }
                            catch (TownyException ex3) {}
                            return;
                        }
                        ++index;
                        TownyMessaging.sendDebugMsg("A jail spawn was skipped because the plot has fallen in war.");
                    }
                    TownyMessaging.sendPrefixedTownMessage(town, TownySettings.getWarPlayerCannotBeJailedPlotFallenMsg());
                    return;
                }
                defenderResident.setJailed(defenderResident, 1, town);
            }
        }
    }
}
