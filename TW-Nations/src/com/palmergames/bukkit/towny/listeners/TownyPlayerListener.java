package com.palmergames.bukkit.towny.listeners;

import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.ChatColor;
import org.bukkit.event.entity.PlayerDeathEvent;
import com.palmergames.bukkit.towny.event.PlayerLeaveTownEvent;
import com.palmergames.bukkit.towny.event.PlayerEnterTownEvent;
import org.bukkit.Bukkit;
import com.palmergames.bukkit.towny.event.PlayerChangePlotEvent;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerFishEvent;
import com.palmergames.bukkit.towny.war.eventwar.WarUtil;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockType;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.material.Sign;
import org.bukkit.event.player.PlayerTeleportEvent;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import com.palmergames.bukkit.towny.object.PlayerCache;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.block.BlockFace;
import com.palmergames.bukkit.towny.object.TownyWorld;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import com.palmergames.bukkit.towny.TownyMessaging;
import java.util.Arrays;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.towny.object.TownyPermission;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import org.bukkit.Location;
import com.palmergames.bukkit.towny.TownySettings;
import org.bukkit.event.player.PlayerRespawnEvent;
import com.palmergames.bukkit.towny.permissions.TownyPerms;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyTimerHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.event.player.PlayerJoinEvent;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.event.Listener;

public class TownyPlayerListener implements Listener
{
    private final Towny plugin;
    
    public TownyPlayerListener(final Towny instance) {
        this.plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (this.plugin.isError()) {
            player.sendMessage("§c[Towny Error] Locked in Safe mode!");
            return;
        }
        TownyUniverse.getInstance().onLogin(player);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        if (this.plugin.isError()) {
            return;
        }
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        townyUniverse.onLogout(event.getPlayer());
        try {
            if (TownyTimerHandler.isTeleportWarmupRunning()) {
                TownyAPI.getInstance().abortTeleportRequest(townyUniverse.getDataSource().getResident(event.getPlayer().getName().toLowerCase()));
            }
        }
        catch (NotRegisteredException ex) {}
        this.plugin.deleteCache(event.getPlayer());
        TownyPerms.removeAttachment(event.getPlayer().getName());
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        if (this.plugin.isError()) {
            return;
        }
        final Player player = event.getPlayer();
        if (!TownySettings.isTownRespawning()) {
            return;
        }
        final Location respawn = TownyAPI.getInstance().getTownSpawnLocation(player);
        if (respawn == null) {
            return;
        }
        if (TownySettings.isTownRespawningInOtherWorlds() && !player.getWorld().equals(respawn.getWorld())) {
            return;
        }
        if (TownySettings.getBedUse() && player.getBedSpawnLocation() != null) {
            event.setRespawnLocation(player.getBedSpawnLocation());
        }
        else {
            event.setRespawnLocation(respawn);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJailRespawn(final PlayerRespawnEvent event) {
        if (this.plugin.isError()) {
            return;
        }
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (!TownySettings.isTownRespawning()) {
            return;
        }
        try {
            Location respawn = null;
            final Resident resident = townyUniverse.getDataSource().getResident(event.getPlayer().getName());
            if (resident.isJailed()) {
                final Town respawnTown = townyUniverse.getDataSource().getTown(resident.getJailTown());
                respawn = respawnTown.getJailSpawn(resident.getJailSpawn());
                event.setRespawnLocation(respawn);
            }
        }
        catch (TownyException ex) {}
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketEmpty(final PlayerBucketEmptyEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(this.onPlayerInteract(event.getPlayer(), event.getBlockClicked().getRelative(event.getBlockFace()), event.getPlayer().getInventory().getItemInMainHand()));
        if (!event.isCancelled()) {
            event.setCancelled(this.onPlayerInteract(event.getPlayer(), event.getBlockClicked(), event.getItemStack()));
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerBucketFill(final PlayerBucketFillEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(this.onPlayerInteract(event.getPlayer(), event.getBlockClicked(), event.getItemStack()));
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        final Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        TownyWorld World = null;
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            World = townyUniverse.getDataSource().getWorld(block.getLocation().getWorld().getName());
            if (!World.isUsingTowny()) {
                return;
            }
        }
        catch (NotRegisteredException e) {
            e.printStackTrace();
            return;
        }
        if (event.getAction() == Action.PHYSICAL && block.getType() == Material.SOIL && (World.isDisablePlayerTrample() || !PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.DESTROY))) {
            event.setCancelled(true);
            return;
        }
        if (event.hasItem()) {
            if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.getMaterial(TownySettings.getTool()) && townyUniverse.getPermissionSource().isTownyAdmin(player) && event.getClickedBlock() != null) {
                block = event.getClickedBlock();
                if (block.getState().getData() instanceof Sign) {
                	final Sign sign = (Sign)block.getState().getData();
                    final BlockFace facing = sign.getFacing();
                    final BlockFace attachedFace = sign.getAttachedFace();
                    TownyMessaging.sendMessage(player, Arrays.asList(ChatTools.formatTitle("Sign Info"), ChatTools.formatCommand("", "Sign Type", "", block.getType().name()), ChatTools.formatCommand("", "Facing", "", facing.toString()), ChatTools.formatCommand("", "AttachedFace", "", attachedFace.toString())));
                }
                else if (block.getState().getData() instanceof Door) {
                    final Door door = (Door)block.getState().getData();
                    if (door.isTopHalf()) {
                        door.getHinge();
                        final Door otherdoor = (Door)block.getRelative(BlockFace.DOWN).getState().getData();
                        otherdoor.isOpen();
                        otherdoor.getFacing();
                    }
                    else {
                        door.isOpen();
                        door.getFacing();
                        final Door otherdoor = (Door)block.getRelative(BlockFace.UP).getState().getData();
                        otherdoor.getHinge();
                    }
                    TownyMessaging.sendMessage(player, Arrays.asList(ChatTools.formatTitle("Door Info"), ChatTools.formatCommand("", "Door Type", "", block.getType().name()), ChatTools.formatCommand("", "hinged on ", "", String.valueOf(door.getHinge())), ChatTools.formatCommand("", "isOpen", "", String.valueOf(door.isOpen())), ChatTools.formatCommand("", "getFacing", "", door.getFacing().name())));
                }
                else {
                	TownyMessaging.sendMessage(player, Arrays.asList(ChatTools.formatTitle("Block Info"), ChatTools.formatCommand("", "Material", "", block.getType().name()), ChatTools.formatCommand("", "MaterialData", "", block.getType().getData().toString())));
                }
                event.setUseInteractedBlock(Event.Result.DENY);
                event.setCancelled(true);
            }
            if (TownySettings.isItemUseMaterial(event.getItem().getType().name())) {
                TownyMessaging.sendDebugMsg("ItemUse Material found: " + event.getItem().getType().name());
                event.setCancelled(this.onPlayerInteract(player, event.getClickedBlock(), event.getItem()));
            }
        }
        if (!event.useItemInHand().equals((Object)Event.Result.DENY) && event.getClickedBlock() != null && (TownySettings.isSwitchMaterial(event.getClickedBlock().getType().name()) || event.getAction() == Action.PHYSICAL)) {
            this.onPlayerSwitchEvent(event, null, World);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(final PlayerInteractAtEntityEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        if (event.getRightClicked() != null) {
            if (!TownyAPI.getInstance().isTownyWorld(event.getPlayer().getWorld())) {
                return;
            }
            final Player player = event.getPlayer();
            boolean bBuild = true;
            Material block = null;
            switch (event.getRightClicked().getType()) {
                case ARMOR_STAND: {
                    TownyMessaging.sendDebugMsg("ArmorStand Right Clicked");
                    block = Material.ARMOR_STAND;
                    bBuild = PlayerCacheUtil.getCachePermission(player, event.getRightClicked().getLocation(), block, TownyPermission.ActionType.DESTROY);
                    break;
                }
                case ITEM_FRAME: {
                    TownyMessaging.sendDebugMsg("Item_Frame Right Clicked");
                    block = Material.ITEM_FRAME;
                    bBuild = PlayerCacheUtil.getCachePermission(player, event.getRightClicked().getLocation(), block, TownyPermission.ActionType.SWITCH);
                    break;
                }
            }
            if (block != null) {
                if (bBuild) {
                    return;
                }
                event.setCancelled(true);
                final PlayerCache cache = this.plugin.getCache(player);
                if (cache.hasBlockErrMsg()) {
                    TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
                }
            }
            else if (event.getPlayer().getInventory().getItemInMainHand() != null && TownySettings.isItemUseMaterial(event.getPlayer().getInventory().getItemInMainHand().getType().name())) {
                event.setCancelled(this.onPlayerInteract(event.getPlayer(), null, event.getPlayer().getInventory().getItemInMainHand()));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        if (event.getRightClicked() != null) {
            TownyWorld World = null;
            try {
                World = TownyUniverse.getInstance().getDataSource().getWorld(event.getPlayer().getWorld().getName());
                if (!World.isUsingTowny()) {
                    return;
                }
            }
            catch (NotRegisteredException e) {
                e.printStackTrace();
                return;
            }
            final Player player = event.getPlayer();
            Material block = null;
            TownyPermission.ActionType actionType = TownyPermission.ActionType.SWITCH;
            switch (event.getRightClicked().getType()) {
                case ITEM_FRAME: {
                    block = Material.ITEM_FRAME;
                    actionType = TownyPermission.ActionType.DESTROY;
                    break;
                }
                case PAINTING: {
                    block = Material.PAINTING;
                    actionType = TownyPermission.ActionType.DESTROY;
                    break;
                }
                case MINECART:
                case MINECART_MOB_SPAWNER: {
                    block = Material.MINECART;
                    break;
                }
                case MINECART_CHEST: {
                    block = Material.STORAGE_MINECART;
                    break;
                }
                case MINECART_FURNACE: {
                    block = Material.POWERED_MINECART;
                    break;
                }
                case MINECART_COMMAND: {
                    block = Material.COMMAND_MINECART;
                    break;
                }
                case MINECART_HOPPER: {
                    block = Material.HOPPER_MINECART;
                    break;
                }
                case MINECART_TNT: {
                    block = Material.EXPLOSIVE_MINECART;
                    break;
                }
            }
            if (block != null && TownySettings.isSwitchMaterial(block.name())) {
                if (!PlayerCacheUtil.getCachePermission(player, event.getRightClicked().getLocation(), block, actionType)) {
                    event.setCancelled(true);
                    final PlayerCache cache = this.plugin.getCache(player);
                    if (cache.hasBlockErrMsg()) {
                        TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
                    }
                }
                return;
            }
            if (event.getPlayer().getInventory().getItemInMainHand() != null) {
                if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.getMaterial(TownySettings.getTool())) {
                    if (event.getHand().equals((Object)EquipmentSlot.OFF_HAND)) {
                        return;
                    }
                    final Entity entity = event.getRightClicked();
                    TownyMessaging.sendMessage(player, Arrays.asList(ChatTools.formatTitle("Entity Info"), ChatTools.formatCommand("", "Entity Class", "", entity.getType().getEntityClass().getSimpleName())));
                    event.setCancelled(true);
                }
                if (TownySettings.isItemUseMaterial(event.getPlayer().getInventory().getItemInMainHand().getType().name())) {
                    event.setCancelled(this.onPlayerInteract(event.getPlayer(), null, event.getPlayer().getInventory().getItemInMainHand()));
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getBlockY() == event.getTo().getBlockY()) {
            return;
        }
        final Player player = event.getPlayer();
        final Location to = event.getTo();
        final PlayerCache cache = this.plugin.getCache(player);
        Location from;
        try {
            from = cache.getLastLocation();
        }
        catch (NullPointerException e2) {
            from = event.getFrom();
        }
        if (WorldCoord.cellChanged(from, to)) {
            try {
                final TownyWorld fromWorld = townyUniverse.getDataSource().getWorld(from.getWorld().getName());
                final WorldCoord fromCoord = new WorldCoord(fromWorld.getName(), Coord.parseCoord(from));
                final TownyWorld toWorld = townyUniverse.getDataSource().getWorld(to.getWorld().getName());
                final WorldCoord toCoord = new WorldCoord(toWorld.getName(), Coord.parseCoord(to));
                this.onPlayerMoveChunk(player, fromCoord, toCoord, from, to, event);
            }
            catch (NotRegisteredException e) {
                TownyMessaging.sendErrorMsg(player, e.getMessage());
            }
        }
        cache.setLastLocation(to);
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        try {
            if (TownyUniverse.getInstance().getDataSource().getResident(player.getName()).isJailed()) {
                if (event.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND) {
                    TownyMessaging.sendErrorMsg(event.getPlayer(), String.format(TownySettings.getLangString("msg_err_jailed_players_no_teleport"), new Object[0]));
                    event.setCancelled(true);
                    return;
                }
                if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
                    return;
                }
                if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL || !TownySettings.JailAllowsEnderPearls()) {
                    TownyMessaging.sendErrorMsg(event.getPlayer(), String.format(TownySettings.getLangString("msg_err_jailed_players_no_teleport"), new Object[0]));
                    event.setCancelled(true);
                }
            }
        }
        catch (NotRegisteredException ex) {}
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT && TownySettings.isItemUseMaterial(Material.CHORUS_FRUIT.name()) && this.onPlayerInteract(event.getPlayer(), event.getTo().getBlock(), new ItemStack(Material.CHORUS_FRUIT))) {
            event.setCancelled(true);
            return;
        }
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL && TownySettings.isItemUseMaterial(Material.ENDER_PEARL.name()) && this.onPlayerInteract(event.getPlayer(), event.getTo().getBlock(), new ItemStack(Material.ENDER_PEARL))) {
            event.setCancelled(true);
            TownyMessaging.sendErrorMsg(event.getPlayer(), String.format(TownySettings.getLangString("msg_err_ender_pearls_disabled"), new Object[0]));
            return;
        }
        this.onPlayerMove((PlayerMoveEvent)event);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangeWorld(final PlayerChangedWorldEvent event) {
        if (event.getPlayer().isOnline()) {
            TownyPerms.assignPermissions(null, event.getPlayer());
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBedEnter(final PlayerBedEnterEvent event) {
        if (!TownyAPI.getInstance().isTownyWorld(event.getBed().getWorld())) {
            return;
        }
        if (!TownySettings.getBedUse()) {
            return;
        }
        boolean isOwner = false;
        boolean isInnPlot = false;
        try {
            final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(event.getPlayer().getName());
            final WorldCoord worldCoord = new WorldCoord(event.getPlayer().getWorld().getName(), Coord.parseCoord(event.getBed().getLocation()));
            final TownBlock townblock = worldCoord.getTownBlock();
            isOwner = townblock.isOwner(resident);
            isInnPlot = (townblock.getType() == TownBlockType.INN);
            if (resident.hasNation() && townblock.getTown().hasNation()) {
                final Nation residentNation = resident.getTown().getNation();
                final Nation townblockNation = townblock.getTown().getNation();
                if (townblockNation.hasEnemy(residentNation)) {
                    event.setCancelled(true);
                    TownyMessaging.sendErrorMsg(event.getPlayer(), String.format(TownySettings.getLangString("msg_err_no_sleep_in_enemy_inn"), new Object[0]));
                    return;
                }
            }
        }
        catch (NotRegisteredException ex) {}
        if (!isOwner && !isInnPlot) {
            event.setCancelled(true);
            TownyMessaging.sendErrorMsg(event.getPlayer(), String.format(TownySettings.getLangString("msg_err_cant_use_bed"), new Object[0]));
        }
    }
    
    public boolean onPlayerInteract(final Player player, final Block block, final ItemStack item) {
        boolean cancelState = false;
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        try {
            final String worldName = player.getWorld().getName();
            WorldCoord worldCoord;
            if (block != null) {
                worldCoord = new WorldCoord(worldName, Coord.parseCoord(block));
            }
            else {
                worldCoord = new WorldCoord(worldName, Coord.parseCoord((Entity)player));
            }
            boolean bItemUse;
            if (block != null) {
                bItemUse = PlayerCacheUtil.getCachePermission(player, block.getLocation(), item.getType(), TownyPermission.ActionType.ITEM_USE);
            }
            else {
                bItemUse = PlayerCacheUtil.getCachePermission(player, player.getLocation(), item.getType(), TownyPermission.ActionType.ITEM_USE);
            }
            final boolean wildOverride = townyUniverse.getPermissionSource().hasWildOverride(worldCoord.getTownyWorld(), player, item.getType(), TownyPermission.ActionType.ITEM_USE);
            final PlayerCache cache = this.plugin.getCache(player);
            try {
                final PlayerCache.TownBlockStatus status = cache.getStatus();
                if (status == PlayerCache.TownBlockStatus.UNCLAIMED_ZONE && wildOverride) {
                    return cancelState;
                }
                if ((status == PlayerCache.TownBlockStatus.TOWN_RESIDENT && townyUniverse.getPermissionSource().hasOwnTownOverride(player, item.getType(), TownyPermission.ActionType.ITEM_USE)) || ((status == PlayerCache.TownBlockStatus.OUTSIDER || status == PlayerCache.TownBlockStatus.TOWN_ALLY || status == PlayerCache.TownBlockStatus.ENEMY) && townyUniverse.getPermissionSource().hasAllTownOverride(player, item.getType(), TownyPermission.ActionType.ITEM_USE))) {
                    return cancelState;
                }
                if ((status == PlayerCache.TownBlockStatus.WARZONE && TownyWarConfig.isAllowingAttacks()) || (TownyAPI.getInstance().isWarTime() && status == PlayerCache.TownBlockStatus.WARZONE && !WarUtil.isPlayerNeutral(player))) {
                    if (!TownyWarConfig.isAllowingItemUseInWarZone()) {
                        cancelState = true;
                        TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_warzone_cannot_use_item"));
                    }
                    return cancelState;
                }
                if ((status == PlayerCache.TownBlockStatus.UNCLAIMED_ZONE && !wildOverride) || (!bItemUse && status != PlayerCache.TownBlockStatus.UNCLAIMED_ZONE)) {
                    cancelState = true;
                }
                if (cache.hasBlockErrMsg()) {
                    TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
                }
            }
            catch (NullPointerException e) {
                System.out.print("NPE generated!");
                System.out.print("Player: " + player.getName());
                System.out.print("Item: " + item.getType().name());
            }
        }
        catch (NotRegisteredException e2) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_not_configured"));
            cancelState = true;
            return cancelState;
        }
        return cancelState;
    }
    
    public void onPlayerSwitchEvent(final PlayerInteractEvent event, final String errMsg, final TownyWorld world) {
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        event.setCancelled(this.onPlayerSwitchEvent(player, block, errMsg, world));
    }
    
    public boolean onPlayerSwitchEvent(final Player player, final Block block, final String errMsg, final TownyWorld world) {
        if (!TownySettings.isSwitchMaterial(block.getType().name())) {
            return false;
        }
        final boolean bSwitch = PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.SWITCH);
        if (bSwitch) {
            return false;
        }
        final PlayerCache cache = this.plugin.getCache(player);
        final PlayerCache.TownBlockStatus status = cache.getStatus();
        if ((status != PlayerCache.TownBlockStatus.WARZONE || !TownyWarConfig.isAllowingAttacks()) && (!TownyAPI.getInstance().isWarTime() || status != PlayerCache.TownBlockStatus.WARZONE || WarUtil.isPlayerNeutral(player))) {
            if (cache.hasBlockErrMsg()) {
                TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
            }
            return true;
        }
        if (!TownyWarConfig.isAllowingSwitchesInWarZone()) {
            TownyMessaging.sendErrorMsg(player, TownySettings.getLangString("msg_err_warzone_cannot_use_switches"));
            return true;
        }
        return false;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerFishEvent(final PlayerFishEvent event) {
        if (!TownyAPI.getInstance().isTownyWorld(event.getPlayer().getWorld())) {
            return;
        }
        if (event.getState().equals((Object)PlayerFishEvent.State.CAUGHT_ENTITY)) {
            final Player player = event.getPlayer();
            final Entity caught = event.getCaught();
            if (caught.getType().equals((Object)EntityType.PLAYER)) {
                return;
            }
            final boolean bDestroy = PlayerCacheUtil.getCachePermission(player, caught.getLocation(), Material.GRASS, TownyPermission.ActionType.DESTROY);
            if (!bDestroy) {
                event.setCancelled(true);
                event.getHook().remove();
            }
        }
    }
    
    public void onPlayerMoveChunk(final Player player, final WorldCoord from, final WorldCoord to, final Location fromLoc, final Location toLoc, final PlayerMoveEvent moveEvent) {
        this.plugin.getCache(player).setLastLocation(toLoc);
        this.plugin.getCache(player).updateCoord(to);
        final PlayerChangePlotEvent event = new PlayerChangePlotEvent(player, from, to, moveEvent);
        Bukkit.getServer().getPluginManager().callEvent((Event)event);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangePlotEvent(final PlayerChangePlotEvent event) {
        final PlayerMoveEvent pme = event.getMoveEvent();
        final Player player = event.getPlayer();
        final WorldCoord from = event.getFrom();
        final WorldCoord to = event.getTo();
        try {
            TownyUniverse.getInstance().getDataSource().getResident(player.getName());
            try {
                to.getTownBlock();
                if (to.getTownBlock().hasTown()) {
                    try {
                        final Town fromTown = from.getTownBlock().getTown();
                        if (!to.getTownBlock().getTown().equals(fromTown)) {
                            Bukkit.getServer().getPluginManager().callEvent((Event)new PlayerEnterTownEvent(player, to, from, to.getTownBlock().getTown(), pme));
                            Bukkit.getServer().getPluginManager().callEvent((Event)new PlayerLeaveTownEvent(player, to, from, from.getTownBlock().getTown(), pme));
                        }
                    }
                    catch (NotRegisteredException e) {
                        Bukkit.getServer().getPluginManager().callEvent((Event)new PlayerEnterTownEvent(player, to, from, to.getTownBlock().getTown(), pme));
                    }
                }
                else if (from.getTownBlock().hasTown() && !to.getTownBlock().hasTown()) {
                    Bukkit.getServer().getPluginManager().callEvent((Event)new PlayerLeaveTownEvent(player, to, from, from.getTownBlock().getTown(), pme));
                }
            }
            catch (NotRegisteredException e) {
                Bukkit.getServer().getPluginManager().callEvent((Event)new PlayerLeaveTownEvent(player, to, from, from.getTownBlock().getTown(), pme));
            }
        }
        catch (NotRegisteredException ex) {}
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onOutlawEnterTown(final PlayerEnterTownEvent event) throws NotRegisteredException {
        final Player player = event.getPlayer();
        final WorldCoord to = event.getTo();
        final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());
        if (to.getTownBlock().getTown().hasOutlaw(resident)) {
            TownyMessaging.sendMsg(player, String.format(TownySettings.getLangString("msg_you_are_an_outlaw_in_this_town"), to.getTownBlock().getTown()));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDieInTown(final PlayerDeathEvent event) {
        final boolean keepInventory = event.getKeepInventory();
        final boolean keepLevel = event.getKeepLevel();
        final Player player = event.getEntity();
        final Location deathloc = player.getLocation();
        if (TownySettings.getKeepInventoryInTowns() && !keepInventory) {
            final TownBlock tb = TownyAPI.getInstance().getTownBlock(deathloc);
            if (tb != null && tb.hasTown()) {
                event.setKeepInventory(true);
                event.getDrops().clear();
            }
        }
        if (TownySettings.getKeepExperienceInTowns() && !keepLevel) {
            final TownBlock tb = TownyAPI.getInstance().getTownBlock(deathloc);
            if (tb != null && tb.hasTown()) {
                event.setKeepLevel(true);
                event.setDroppedExp(0);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerEnterTown(final PlayerEnterTownEvent event) throws TownyException {
        final Resident resident = TownyUniverse.getInstance().getDataSource().getResident(event.getPlayer().getName());
        final WorldCoord to = event.getTo();
        if (TownySettings.isNotificationUsingTitles()) {
            String title = ChatColor.translateAlternateColorCodes('&', TownySettings.getNotificationTitlesTownTitle());
            String subtitle = ChatColor.translateAlternateColorCodes('&', TownySettings.getNotificationTitlesTownSubtitle());
            if (title.contains("{townname}")) {
                title = title.replace("{townname}", to.getTownBlock().getTown().getName());
            }
            if (subtitle.contains("{townname}")) {
                subtitle = subtitle.replace("{townname}", to.getTownBlock().getTown().getName());
            }
            TownyMessaging.sendTitleMessageToResident(resident, title, subtitle);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeaveTown(final PlayerLeaveTownEvent event) throws TownyException {
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final Resident resident = townyUniverse.getDataSource().getResident(event.getPlayer().getName());
        final WorldCoord to = event.getTo();
        if (TownySettings.isNotificationUsingTitles()) {
            try {
                to.getTownBlock().getTown();
            }
            catch (NotRegisteredException e) {
                String title = ChatColor.translateAlternateColorCodes('&', TownySettings.getNotificationTitlesWildTitle());
                String subtitle = ChatColor.translateAlternateColorCodes('&', TownySettings.getNotificationTitlesWildSubtitle());
                if (title.contains("{wilderness}")) {
                    title = title.replace("{wilderness}", townyUniverse.getDataSource().getWorld(event.getPlayer().getLocation().getWorld().getName()).getUnclaimedZoneName());
                }
                if (subtitle.contains("{wilderness}")) {
                    subtitle = subtitle.replace("{wilderness}", townyUniverse.getDataSource().getWorld(event.getPlayer().getLocation().getWorld().getName()).getUnclaimedZoneName());
                }
                TownyMessaging.sendTitleMessageToResident(resident, title, subtitle);
            }
        }
        final Player player = event.getPlayer();
        if (townyUniverse.getDataSource().getResident(player.getName()).isJailed()) {
            resident.freeFromJail(player, resident.getJailSpawn(), true);
            townyUniverse.getDataSource().saveResident(resident);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onJailedPlayerUsesCommand(final PlayerCommandPreprocessEvent event) throws NotRegisteredException {
        if (!TownyAPI.getInstance().getDataSource().getResident(event.getPlayer().getName()).isJailed()) {
            return;
        }
        final String[] split = event.getMessage().substring(1).split(" ");
        if (TownySettings.getJailBlacklistedCommands().contains(split[0])) {
            TownyMessaging.sendErrorMsg(event.getPlayer(), TownySettings.getLangString("msg_you_cannot_use_that_command_while_jailed"));
            event.setCancelled(true);
        }
    }
}
