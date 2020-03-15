// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.listeners;

import org.bukkit.plugin.Plugin;
import com.palmergames.bukkit.towny.tasks.ProtectionRegenTask;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import com.palmergames.bukkit.towny.regen.block.BlockLocation;
import org.bukkit.event.block.BlockExplodeEvent;
import com.palmergames.bukkit.towny.war.eventwar.War;
import org.bukkit.Material;
import com.palmergames.bukkit.towny.object.TownBlock;
import org.bukkit.Location;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarBlockUtil;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockPistonExtendEvent;
import java.util.Iterator;
import java.util.List;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockBurnEvent;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.war.flagwar.TownyWar;
import com.palmergames.bukkit.towny.object.WorldCoord;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.war.siegewar.SiegeWarPlaceBlockController;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.war.eventwar.WarUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;
import com.palmergames.bukkit.towny.object.PlayerCache;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.war.siegewar.SiegeWarBreakBlockController;
import com.palmergames.bukkit.towny.TownySettings;
import org.bukkit.event.block.BlockBreakEvent;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.event.Listener;

public class TownyBlockListener implements Listener
{
    private final Towny plugin;
    
    public TownyBlockListener(final Towny instance) {
        this.plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        if (TownySettings.getWarSiegeEnabled()) {
            final boolean skipPermChecks = SiegeWarBreakBlockController.evaluateSiegeWarBreakBlockRequest(player, block, event);
            if (skipPermChecks) {
                return;
            }
        }
        final boolean bDestroy = PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.DESTROY);
        if (bDestroy) {
            return;
        }
        final PlayerCache cache = this.plugin.getCache(player);
        if ((cache.getStatus() == PlayerCache.TownBlockStatus.WARZONE && TownyWarConfig.isAllowingAttacks()) || (TownyAPI.getInstance().isWarTime() && cache.getStatus() == PlayerCache.TownBlockStatus.WARZONE && !WarUtil.isPlayerNeutral(player))) {
            if (!TownyWarConfig.isEditableMaterialInWarZone(block.getType())) {
                event.setCancelled(true);
                TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_warzone_cannot_edit_material"), "destroy", block.getType().toString().toLowerCase()));
            }
            return;
        }
        event.setCancelled(true);
        if (cache.hasBlockErrMsg() && event.isCancelled()) {
            TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            if (TownySettings.getWarSiegeEnabled()) {
                final boolean skipPermChecks = SiegeWarPlaceBlockController.evaluateSiegeWarPlaceBlockRequest(player, block, event, this.plugin);
                if (skipPermChecks) {
                    return;
                }
            }
            final TownyWorld world = townyUniverse.getDataSource().getWorld(block.getWorld().getName());
            final WorldCoord worldCoord = new WorldCoord(world.getName(), Coord.parseCoord(block));
            final boolean bBuild = PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.BUILD);
            if (bBuild) {
                return;
            }
            final PlayerCache cache = this.plugin.getCache(player);
            final PlayerCache.TownBlockStatus status = cache.getStatus();
            if (status == PlayerCache.TownBlockStatus.ENEMY && TownyWarConfig.isAllowingAttacks() && event.getBlock().getType() == TownyWarConfig.getFlagBaseMaterial()) {
                try {
                    if (TownyWar.callAttackCellEvent(this.plugin, player, block, worldCoord)) {
                        return;
                    }
                }
                catch (TownyException e) {
                    TownyMessaging.sendErrorMsg(player, e.getMessage());
                }
                event.setBuild(false);
                event.setCancelled(true);
            }
            else {
                if ((status == PlayerCache.TownBlockStatus.WARZONE && TownyWarConfig.isAllowingAttacks()) || (TownyAPI.getInstance().isWarTime() && cache.getStatus() == PlayerCache.TownBlockStatus.WARZONE && !WarUtil.isPlayerNeutral(player))) {
                    if (!TownyWarConfig.isEditableMaterialInWarZone(block.getType())) {
                        event.setBuild(false);
                        event.setCancelled(true);
                        TownyMessaging.sendErrorMsg(player, String.format(TownySettings.getLangString("msg_err_warzone_cannot_edit_material"), "build", block.getType().toString().toLowerCase()));
                    }
                    return;
                }
                event.setBuild(false);
                event.setCancelled(true);
            }
            if (cache.hasBlockErrMsg() && event.isCancelled()) {
                TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
            }
        }
        catch (NotRegisteredException e2) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_not_configured"));
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBurn(final BlockBurnEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        if (this.onBurn(event.getBlock())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockIgnite(final BlockIgniteEvent event) {
        if (event.isCancelled() || this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        if (this.onBurn(event.getBlock())) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPistonRetract(final BlockPistonRetractEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        final List<Block> blocks = (List<Block>)event.getBlocks();
        if (this.testBlockMove(event.getBlock(), event.getDirection(), true)) {
            event.setCancelled(true);
        }
        if (!blocks.isEmpty()) {
            for (final Block block : blocks) {
                if (this.testBlockMove(block, event.getDirection(), false)) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPistonExtend(final BlockPistonExtendEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        if (this.testBlockMove(event.getBlock(), event.getDirection(), false)) {
            event.setCancelled(true);
        }
        final List<Block> blocks = (List<Block>)event.getBlocks();
        if (!blocks.isEmpty()) {
            for (final Block block : blocks) {
                if (this.testBlockMove(block, event.getDirection(), false)) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    private boolean testBlockMove(final Block block, final BlockFace direction, final boolean pistonBlock) {
        Block blockTo = null;
        if (!pistonBlock) {
            blockTo = block.getRelative(direction);
        }
        else {
            blockTo = block.getRelative(direction.getOppositeFace());
        }
        if (TownySettings.getWarSiegeEnabled() && (SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(block) || SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(blockTo))) {
            return true;
        }
        final Location loc = block.getLocation();
        final Location locTo = blockTo.getLocation();
        final Coord coord = Coord.parseCoord(loc);
        final Coord coordTo = Coord.parseCoord(locTo);
        TownyWorld townyWorld = null;
        TownBlock currentTownBlock = null;
        TownBlock destinationTownBlock = null;
        try {
            townyWorld = TownyUniverse.getInstance().getDataSource().getWorld(loc.getWorld().getName());
            currentTownBlock = townyWorld.getTownBlock(coord);
        }
        catch (NotRegisteredException ex) {}
        try {
            destinationTownBlock = townyWorld.getTownBlock(coordTo);
        }
        catch (NotRegisteredException ex2) {}
        if (currentTownBlock != destinationTownBlock) {
            if ((currentTownBlock == null && destinationTownBlock != null) || (currentTownBlock != null && destinationTownBlock == null)) {
                return true;
            }
            if (!currentTownBlock.hasResident() && !destinationTownBlock.hasResident()) {
                return false;
            }
            try {
                if ((!currentTownBlock.hasResident() && destinationTownBlock.hasResident()) || (currentTownBlock.hasResident() && !destinationTownBlock.hasResident()) || currentTownBlock.getResident() != destinationTownBlock.getResident() || currentTownBlock.getPlotPrice() != -1.0 || destinationTownBlock.getPlotPrice() != -1.0) {
                    return true;
                }
            }
            catch (NotRegisteredException e) {
                return true;
            }
        }
        return false;
    }
    
    private boolean onBurn(final Block block) {
        if (TownySettings.getWarSiegeEnabled() && SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(block)) {
            return true;
        }
        final Location loc = block.getLocation();
        final Coord coord = Coord.parseCoord(loc);
        try {
            final TownyWorld townyWorld = TownyUniverse.getInstance().getDataSource().getWorld(loc.getWorld().getName());
            if (!townyWorld.isUsingTowny()) {
                return false;
            }
            final TownBlock townBlock = TownyAPI.getInstance().getTownBlock(loc);
            if (block.getRelative(BlockFace.DOWN).getType() != Material.OBSIDIAN && townBlock == null && !townyWorld.isForceFire() && !townyWorld.isFire()) {
                TownyMessaging.sendDebugMsg("onBlockIgnite: Canceled " + block.getType().name() + " from igniting within " + coord.toString() + ".");
                return true;
            }
            try {
                boolean inWarringTown = false;
                if (TownyAPI.getInstance().isWarTime() && townyWorld.hasTownBlock(coord) && War.isWarringTown(townBlock.getTown())) {
                    inWarringTown = true;
                }
                if (townyWorld.isWarZone(coord) || (TownyAPI.getInstance().isWarTime() && inWarringTown)) {
                    if (TownyWarConfig.isAllowingFireInWarZone()) {
                        return false;
                    }
                    TownyMessaging.sendDebugMsg("onBlockIgnite: Canceled " + block.getType().name() + " from igniting within " + coord.toString() + ".");
                    return true;
                }
                else if (townBlock != null && (block.getRelative(BlockFace.DOWN).getType() != Material.OBSIDIAN || block.getRelative(BlockFace.DOWN).getType() != Material.NETHERRACK) && ((!townBlock.getTown().isFire() && !townyWorld.isForceFire() && !townBlock.getPermissions().fire) || (TownyAPI.getInstance().isWarTime() && TownySettings.isAllowWarBlockGriefing() && !townBlock.getTown().hasNation()))) {
                    TownyMessaging.sendDebugMsg("onBlockIgnite: Canceled " + block.getType().name() + " from igniting within " + coord.toString() + ".");
                    return true;
                }
            }
            catch (TownyException x) {
                if (!townyWorld.isFire()) {
                    TownyMessaging.sendDebugMsg("onBlockIgnite: Canceled " + block.getType().name() + " from igniting within " + coord.toString() + ".");
                    return true;
                }
            }
        }
        catch (NotRegisteredException ex) {}
        return false;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreateExplosion(final BlockExplodeEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        final List<Block> blocks = (List<Block>)event.blockList();
        int count = 0;
        TownyWorld townyWorld;
        try {
            townyWorld = TownyUniverse.getInstance().getDataSource().getWorld(event.getBlock().getLocation().getWorld().getName());
        }
        catch (NotRegisteredException e) {
            e.printStackTrace();
            return;
        }
        for (final Block block : blocks) {
            ++count;
            if (!this.locationCanExplode(townyWorld, block.getLocation())) {
                event.setCancelled(true);
                return;
            }
            if (!TownyAPI.getInstance().isWilderness(block.getLocation()) || !townyWorld.isUsingTowny() || !townyWorld.isExpl() || !townyWorld.isUsingPlotManagementWildRevert() || TownyRegenAPI.hasProtectionRegenTask(new BlockLocation(block.getLocation())) || block.getType() == Material.TNT) {
                continue;
            }
            final ProtectionRegenTask task = new ProtectionRegenTask(this.plugin, block);
            task.setTaskId(this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)task, (TownySettings.getPlotManagementWildRegenDelay() + count) * 20L));
            TownyRegenAPI.addProtectionRegenTask(task);
            event.setYield(0.0f);
            block.getDrops().clear();
        }
    }
    
    public boolean locationCanExplode(final TownyWorld world, final Location target) {
        if (TownySettings.getWarSiegeEnabled() && SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(target.getBlock())) {
            return false;
        }
        final Coord coord = Coord.parseCoord(target);
        if (world.isWarZone(coord) && !TownyWarConfig.isAllowingExplosionsInWarZone()) {
            return false;
        }
        TownBlock townBlock = null;
        boolean isNeutral = false;
        try {
            townBlock = world.getTownBlock(coord);
            if (townBlock.hasTown() && !War.isWarZone(townBlock.getWorldCoord())) {
                isNeutral = true;
            }
        }
        catch (NotRegisteredException e1) {
            if (TownyAPI.getInstance().isWilderness(target.getBlock().getLocation())) {
                isNeutral = !world.isExpl();
                if (!world.isExpl() && !TownyAPI.getInstance().isWarTime()) {
                    return false;
                }
                if (world.isExpl() && !TownyAPI.getInstance().isWarTime()) {
                    return true;
                }
            }
        }
        try {
            if (world.isUsingTowny() && !world.isForceExpl()) {
                if (TownyAPI.getInstance().isWarTime() && TownyWarConfig.explosionsBreakBlocksInWarZone() && !isNeutral) {
                    return true;
                }
                if (!townBlock.getPermissions().explosion || (TownyAPI.getInstance().isWarTime() && TownyWarConfig.isAllowingExplosionsInWarZone() && !townBlock.getTown().hasNation() && !townBlock.getTown().isBANG())) {
                    return false;
                }
            }
        }
        catch (NotRegisteredException e2) {
            return world.isExpl();
        }
        return true;
    }
}
