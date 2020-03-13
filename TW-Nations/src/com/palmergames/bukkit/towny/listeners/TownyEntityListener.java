// 
// Decompiled by Procyon v0.5.36
// 

package com.palmergames.bukkit.towny.listeners;

import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import com.palmergames.bukkit.towny.object.PlayerCache;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.block.ShulkerBox;
import org.bukkit.Keyed;
import org.bukkit.material.Attachable;
import org.bukkit.material.MaterialData;
import org.bukkit.material.PistonBaseMaterial;
import org.bukkit.material.PistonExtensionMaterial;
import org.bukkit.material.PressurePlate;
import org.bukkit.material.Sign;
import org.bukkit.plugin.Plugin;
import com.palmergames.bukkit.towny.tasks.ProtectionRegenTask;
import com.palmergames.bukkit.towny.regen.TownyRegenAPI;
import com.palmergames.bukkit.towny.regen.block.BlockLocation;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;

import java.util.Comparator;
import com.palmergames.bukkit.util.ArraySort;
import org.bukkit.event.entity.EntityExplodeEvent;
import com.palmergames.bukkit.towny.war.siegewar.utils.SiegeWarBlockUtil;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.entity.Creature;
import org.bukkit.event.entity.EntityInteractEvent;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import net.citizensnpcs.api.CitizensAPI;
import com.palmergames.bukkit.towny.tasks.MobRemovalTimerTask;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.PotionSplashEvent;
import com.palmergames.bukkit.towny.object.TownBlock;
import java.util.Iterator;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.potion.PotionEffect;
import java.util.List;
import org.bukkit.block.Block;
import java.util.ArrayList;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.ArmorStand;
import com.palmergames.bukkit.towny.war.flagwar.TownyWarConfig;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.palmergames.bukkit.towny.object.TownyPermission;
import org.bukkit.Material;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import com.palmergames.bukkit.towny.object.TownyWorld;
import org.bukkit.Location;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.Coord;
import org.bukkit.entity.Monster;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.event.EventHandler;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.projectiles.ProjectileSource;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.entity.Player;
import com.palmergames.bukkit.towny.war.eventwar.War;
import com.palmergames.bukkit.towny.TownySettings;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import com.palmergames.bukkit.towny.TownyAPI;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import com.palmergames.bukkit.towny.Towny;
import org.bukkit.event.Listener;

public class TownyEntityListener implements Listener
{
    private final Towny plugin;
    
    public TownyEntityListener(final Towny instance) {
        this.plugin = instance;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) throws Exception {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        Entity attacker = event.getDamager();
        final Entity defender = event.getEntity();
        if (!TownyAPI.getInstance().isWarTime()) {
            if (CombatUtil.preventDamageCall(this.plugin, attacker, defender)) {
                if (attacker instanceof Projectile) {
                    final ProjectileSource shooter = ((Projectile)attacker).getShooter();
                    if (shooter instanceof Entity) {
                        attacker = (Entity)shooter;
                    }
                    else {
                        final BlockProjectileSource bShooter = (BlockProjectileSource)((Projectile)attacker).getShooter();
                        if (TownyAPI.getInstance().getTownBlock(bShooter.getBlock().getLocation()) != null) {
                            final Town bTown = TownyAPI.getInstance().getTownBlock(bShooter.getBlock().getLocation()).getTown();
                            if (!bTown.hasNation() && TownySettings.isWarTimeTownsNeutral()) {
                                event.setCancelled(true);
                                return;
                            }
                            if (bTown.getNation().isNeutral()) {
                                event.setCancelled(true);
                                return;
                            }
                            if (!War.isWarringTown(bTown)) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                }
                if (!(attacker instanceof Player) || !(defender instanceof Player)) {
                    return;
                }
                final TownyUniverse universe = TownyUniverse.getInstance();
                if (!universe.getDataSource().getResident(attacker.getName()).hasTown() || !universe.getDataSource().getResident(defender.getName()).hasTown()) {
                    TownyMessaging.sendMessage(attacker, TownySettings.getWarAPlayerHasNoTownMsg());
                    event.setCancelled(true);
                    return;
                }
                try {
                    final Town attackerTown = universe.getDataSource().getResident(attacker.getName()).getTown();
                    final Town defenderTown = universe.getDataSource().getResident(defender.getName()).getTown();
                    if ((!attackerTown.hasNation() || !defenderTown.hasNation()) && TownySettings.isWarTimeTownsNeutral()) {
                        TownyMessaging.sendMessage(attacker, TownySettings.getWarAPlayerHasNoNationMsg());
                        event.setCancelled(true);
                        return;
                    }
                    if (attackerTown.getNation().isNeutral() || defenderTown.getNation().isNeutral()) {
                        TownyMessaging.sendMessage(attacker, TownySettings.getWarAPlayerHasANeutralNationMsg());
                        event.setCancelled(true);
                        return;
                    }
                    if (!War.isWarringTown(defenderTown) || !War.isWarringTown(attackerTown)) {
                        TownyMessaging.sendMessage(attacker, TownySettings.getWarAPlayerHasBeenRemovedFromWarMsg());
                        event.setCancelled(true);
                        return;
                    }
                    if ((attackerTown.getNation().hasAlly(defenderTown.getNation()) || defenderTown.getNation().hasAlly(attackerTown.getNation())) && !TownySettings.getFriendlyFire()) {
                        TownyMessaging.sendMessage(attacker, TownySettings.getWarAPlayerIsAnAllyMsg());
                        event.setCancelled(true);
                        return;
                    }
                }
                catch (NotRegisteredException e) {}
                if (CombatUtil.preventFriendlyFire((Player)attacker, (Player)defender)) {
                    if (attacker instanceof Projectile) {
                        attacker.remove();
                    }
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDeath(final EntityDeathEvent event) {
        if (this.plugin.isError()) {
            return;
        }
        final Entity entity = (Entity)event.getEntity();
        if (entity instanceof Monster) {
            final Location loc = entity.getLocation();
            try {
                final TownyWorld townyWorld = TownyUniverse.getInstance().getDataSource().getWorld(loc.getWorld().getName());
                if (townyWorld.isUsingTowny() && townyWorld.getTownBlock(Coord.parseCoord(loc)).getType() == TownBlockType.ARENA) {
                    event.getDrops().clear();
                }
            }
            catch (NotRegisteredException ex) {}
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityTargetLivingEntity(final EntityTargetLivingEntityEvent event) {
        if (this.plugin.isError()) {
            return;
        }
        if (!TownyAPI.getInstance().isTownyWorld(event.getEntity().getWorld())) {
            return;
        }
        if (event.getTarget() instanceof Player && event.getReason().equals((Object)EntityTargetEvent.TargetReason.TEMPT)) {
            final Location loc = event.getEntity().getLocation();
            if (TownyAPI.getInstance().isWilderness(loc)) {
                return;
            }
            if (!TownyAPI.getInstance().getTownBlock(loc).hasResident()) {
                return;
            }
            final Player target = (Player)event.getTarget();
            if (!PlayerCacheUtil.getCachePermission(target, loc, Material.DIRT, TownyPermission.ActionType.DESTROY)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(final EntityDamageByEntityEvent event) {
        if (this.plugin.isError()) {
            return;
        }
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        TownyWorld townyWorld = null;
        final Entity entity = event.getEntity();
        final String damager = event.getDamager().getType().name();
        try {
            townyWorld = townyUniverse.getDataSource().getWorld(entity.getWorld().getName());
        }
        catch (NotRegisteredException e) {
            e.printStackTrace();
        }
        if (TownyAPI.getInstance().isWarTime() && !TownyWarConfig.isAllowingExplosionsInWarZone() && entity instanceof Player && damager.equals("PRIMED_TNT")) {
            event.setCancelled(true);
        }
        TownyMessaging.sendDebugMsg("EntityDamageByEntityEvent : entity = " + entity);
        TownyMessaging.sendDebugMsg("EntityDamageByEntityEvent : damager = " + damager);
        if (entity instanceof ArmorStand || entity instanceof ItemFrame || entity instanceof EnderCrystal || (TownySettings.getEntityTypes().contains("Animals") && entity instanceof Animals) || (TownySettings.getEntityTypes().contains("Villager") && entity instanceof Villager)) {
            if (damager.equals("PRIMED_TNT") || damager.equals("MINECART_TNT") || damager.equals("WITHER_SKULL") || damager.equals("FIREBALL") || damager.equals("SMALL_FIREBALL") || damager.equals("LARGE_FIREBALL") || damager.equals("WITHER") || damager.equals("CREEPER") || damager.equals("FIREWORK")) {
                if (!this.locationCanExplode(townyWorld, entity.getLocation())) {
                    event.setCancelled(true);
                }
                return;
            }
            else if (damager.equals("LIGHTNING")) {
                if (!this.locationCanExplode(townyWorld, entity.getLocation())) {
                    event.setDamage(0.0);
                    event.setCancelled(true);
                }
                return;
            }
            else {
                if (event.getDamager() instanceof Projectile) {
                    try {
                        townyWorld = townyUniverse.getDataSource().getWorld(entity.getWorld().getName());
                    }
                    catch (NotRegisteredException e) {
                        e.printStackTrace();
                    }
                    Object remover = event.getDamager();
                    remover = ((Projectile)remover).getShooter();
                    if (remover instanceof Monster) {
                        event.setCancelled(true);
                    }
                    else if (remover instanceof Player) {
                        final Player player = (Player)remover;
                        final Coord coord = Coord.parseCoord(entity);
                        try {
                            townyWorld.getTownBlock(coord);
                        }
                        catch (NotRegisteredException ex) {
                            return;
                        }
                        final boolean bDestroy = PlayerCacheUtil.getCachePermission(player, entity.getLocation(), Material.ARMOR_STAND, TownyPermission.ActionType.DESTROY);
                        if (bDestroy) {
                            return;
                        }
                        event.setCancelled(true);
                    }
                }
                if (event.getDamager() instanceof Player) {
                    final Player player2 = (Player)event.getDamager();
                    boolean bDestroy2 = false;
                    if (entity instanceof EnderCrystal) {
                        bDestroy2 = PlayerCacheUtil.getCachePermission(player2, entity.getLocation(), Material.GRASS, TownyPermission.ActionType.DESTROY);
                        if (bDestroy2) {
                            return;
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }
        if (damager.equals("FIREWORK") && (!this.locationCanExplode(townyWorld, entity.getLocation()) || CombatUtil.preventPvP(townyWorld, TownyAPI.getInstance().getTownBlock(entity.getLocation())))) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLingeringPotionSplashEvent(final LingeringPotionSplashEvent event) {
        final ThrownPotion potion = (ThrownPotion)event.getEntity();
        final Location loc = potion.getLocation();
        TownyWorld townyWorld = null;
        try {
            townyWorld = TownyUniverse.getInstance().getDataSource().getWorld(loc.getWorld().getName());
        }
        catch (NotRegisteredException e2) {
            return;
        }
        final float radius = event.getAreaEffectCloud().getRadius();
        final List<Block> blocks = new ArrayList<Block>();
        for (double x = loc.getX() - radius; x < loc.getX() + radius; ++x) {
            for (double z = loc.getZ() - radius; z < loc.getZ() + radius; ++z) {
                final Location loc2 = new Location(potion.getWorld(), x, loc.getY(), z);
                final Block b = loc2.getBlock();
                if (b.getType().equals((Object)Material.AIR)) {
                    blocks.add(b);
                }
            }
        }
        final List<PotionEffect> effects = (List<PotionEffect>)potion.getEffects();
        boolean detrimental = false;
        final List<String> prots = TownySettings.getPotionTypes();
        for (final PotionEffect effect : effects) {
            if (prots.contains(effect.getType().getName())) {
                detrimental = true;
            }
        }
        final Object source = potion.getShooter();
        if (!(source instanceof Entity)) {
            return;
        }
        for (final Block block : blocks) {
            final Coord coord = Coord.parseCoord(block.getLocation());
            if (townyWorld.hasTownBlock(coord)) {
                TownBlock townBlock = null;
                try {
                    townBlock = townyWorld.getTownBlock(coord);
                }
                catch (NotRegisteredException e) {
                    e.printStackTrace();
                }
                if (!TownyAPI.getInstance().isWarTime() && CombatUtil.preventPvP(townyWorld, townBlock) && detrimental) {
                    event.setCancelled(true);
                    break;
                }
                continue;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionSplashEvent(final PotionSplashEvent event) {
        final List<LivingEntity> affectedEntities = (List<LivingEntity>)event.getAffectedEntities();
        final ThrownPotion potion = event.getPotion();
        final List<PotionEffect> effects = (List<PotionEffect>)potion.getEffects();
        boolean detrimental = false;
        final List<String> prots = TownySettings.getPotionTypes();
        for (final PotionEffect effect : effects) {
            if (prots.contains(effect.getType().getName())) {
                detrimental = true;
            }
        }
        final Object source = potion.getShooter();
        if (!(source instanceof Entity)) {
            return;
        }
        final Entity attacker = (Entity)source;
        if (!TownyAPI.getInstance().isWarTime()) {
            for (final LivingEntity defender : affectedEntities) {
                if (attacker != defender && CombatUtil.preventDamageCall(this.plugin, attacker, (Entity)defender) && detrimental) {
                    event.setIntensity(defender, -1.0);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(final CreatureSpawnEvent event) throws NotRegisteredException {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntity() != null) {
            final LivingEntity livingEntity = event.getEntity();
            final Location loc = event.getLocation();
            final Coord coord = Coord.parseCoord(loc);
            TownyWorld townyWorld = null;
            try {
                townyWorld = TownyUniverse.getInstance().getDataSource().getWorld(loc.getWorld().getName());
            }
            catch (NotRegisteredException e) {
                return;
            }
            if (townyWorld.isUsingTowny()) {
                if (!townyWorld.hasWorldMobs() && MobRemovalTimerTask.isRemovingWorldEntity(livingEntity)) {
                    if (this.plugin.isCitizens2()) {
                        if (!CitizensAPI.getNPCRegistry().isNPC((Entity)livingEntity)) {
                            event.setCancelled(true);
                        }
                    }
                    else {
                        event.setCancelled(true);
                    }
                }
                if (livingEntity instanceof Villager && !((Villager)livingEntity).isAdult() && TownySettings.isRemovingVillagerBabiesWorld()) {
                    event.setCancelled(true);
                }
            }
            if (!townyWorld.hasTownBlock(coord)) {
                return;
            }
            final TownBlock townBlock = townyWorld.getTownBlock(coord);
            try {
                if (townyWorld.isUsingTowny() && !townyWorld.isForceTownMobs() && !townBlock.getTown().hasMobs() && !townBlock.getPermissions().mobs && MobRemovalTimerTask.isRemovingTownEntity(livingEntity)) {
                    if (this.plugin.isCitizens2()) {
                        if (!CitizensAPI.getNPCRegistry().isNPC((Entity)livingEntity)) {
                            event.setCancelled(true);
                        }
                    }
                    else {
                        event.setCancelled(true);
                    }
                }
                if (livingEntity instanceof Villager && !((Villager)livingEntity).isAdult() && TownySettings.isRemovingVillagerBabiesTown()) {
                    event.setCancelled(true);
                }
            }
            catch (TownyException ex) {}
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(final EntityInteractEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        final Block block = event.getBlock();
        final Entity entity = event.getEntity();
        final List<Entity> passengers = (List<Entity>)entity.getPassengers();
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        final TownyWorld World = null;
        if (!TownyAPI.getInstance().isTownyWorld(event.getBlock().getWorld())) {
            return;
        }
        try {
            final TownyWorld townyWorld = townyUniverse.getDataSource().getWorld(block.getLocation().getWorld().getName());
            if (townyWorld.isDisableCreatureTrample() && (block.getType() == Material.SOIL || block.getType() == Material.WHEAT) && entity instanceof Creature) {
                event.setCancelled(true);
                return;
            }
            if (passengers != null) {
                for (final Entity passenger : passengers) {
                    if (!passenger.getType().equals((Object)EntityType.PLAYER)) {
                        return;
                    }
                    if (TownySettings.isSwitchMaterial(block.getType().name()) && !this.plugin.getPlayerListener().onPlayerSwitchEvent((Player)passenger, block, null, World)) {
                        return;
                    }
                }
            }
            if (TownySettings.isCreatureTriggeringPressurePlateDisabled() && block.getType() == Material.STONE_PLATE && entity instanceof Creature) {
                event.setCancelled(true);
            }
        }
        catch (NotRegisteredException e) {
            e.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityChangeBlockEvent(final EntityChangeBlockEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        final TownyUniverse townyUniverse = TownyUniverse.getInstance();
        switch (event.getEntity().getType()) {
            case WITHER: {
                try {
                    final TownyWorld townyWorld = townyUniverse.getDataSource().getWorld(event.getBlock().getWorld().getName());
                    if (!townyWorld.isUsingTowny()) {
                        return;
                    }
                    if (!this.locationCanExplode(townyWorld, event.getBlock().getLocation())) {
                        event.setCancelled(true);
                        return;
                    }
                }
                catch (NotRegisteredException ex) {}
                break;
            }
            case ENDERMAN: {
                try {
                    final TownyWorld townyWorld = townyUniverse.getDataSource().getWorld(event.getBlock().getWorld().getName());
                    if (!townyWorld.isUsingTowny()) {
                        return;
                    }
                    if (townyWorld.isEndermanProtect()) {
                        event.setCancelled(true);
                    }
                }
                catch (NotRegisteredException ex2) {}
                break;
            }
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
        try {
            final TownBlock townBlock = world.getTownBlock(coord);
            if (world.isUsingTowny() && !world.isForceExpl() && (!townBlock.getPermissions().explosion || (TownyAPI.getInstance().isWarTime() && TownySettings.isAllowWarBlockGriefing() && !townBlock.getTown().hasNation() && !townBlock.getTown().isBANG()))) {
                return false;
            }
        }
        catch (NotRegisteredException e) {
            return world.isExpl();
        }
        return true;
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(final EntityExplodeEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        TownyWorld townyWorld;
        try {
            townyWorld = TownyUniverse.getInstance().getDataSource().getWorld(event.getLocation().getWorld().getName());
            if (!townyWorld.isUsingTowny()) {
                return;
            }
        }
        catch (NotRegisteredException e) {
            return;
        }
        final List<Block> blocks = (List<Block>)event.blockList();
        final Entity entity = event.getEntity();
        blocks.sort(ArraySort.getInstance());
        if (TownyAPI.getInstance().isWarTime()) {
            final Iterator<Block> it = event.blockList().iterator();
            int count = 0;
            while (it.hasNext()) {
                final Block block = it.next();
                TownBlock townBlock = null;
                boolean isNeutralTownBlock = false;
                ++count;
                try {
                    townBlock = townyWorld.getTownBlock(Coord.parseCoord(block.getLocation()));
                    if (townBlock.hasTown() && !War.isWarringTown(townBlock.getTown())) {
                        isNeutralTownBlock = true;
                    }
                }
                catch (NotRegisteredException ex) {}
                if (!isNeutralTownBlock) {
                    if (!TownyWarConfig.isAllowingExplosionsInWarZone()) {
                        if (event.getEntity() != null) {
                            TownyMessaging.sendDebugMsg("onEntityExplode: Canceled " + event.getEntity().getEntityId() + " from exploding within " + Coord.parseCoord(block.getLocation()).toString() + ".");
                        }
                        event.setCancelled(true);
                        return;
                    }
                    event.setCancelled(false);
                    if (TownyWarConfig.explosionsBreakBlocksInWarZone()) {
                        if (TownyWarConfig.getExplosionsIgnoreList().contains(block.getType().toString()) || TownyWarConfig.getExplosionsIgnoreList().contains(block.getRelative(BlockFace.UP).getType().toString())) {
                            it.remove();
                        }
                        else {
                            if (!TownyWarConfig.regenBlocksAfterExplosionInWarZone() || TownyRegenAPI.hasProtectionRegenTask(new BlockLocation(block.getLocation())) || block.getType() == Material.TNT) {
                                continue;
                            }
                            final ProtectionRegenTask task = new ProtectionRegenTask(this.plugin, block);
                            task.setTaskId(this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)task, (TownySettings.getPlotManagementWildRegenDelay() + count) * 20L));
                            TownyRegenAPI.addProtectionRegenTask(task);
                            event.setYield(0.0f);
                            block.getDrops().clear();
                        }
                    }
                    else {
                        event.blockList().remove(block);
                    }
                }
                else {
                    if (townyWorld.isForceExpl()) {
                        continue;
                    }
                    try {
                        if ((!townBlock.getPermissions().explosion || (TownySettings.isAllowWarBlockGriefing() && !townBlock.getTown().isBANG())) && event.getEntity() != null) {
                            event.setCancelled(true);
                            return;
                        }
                        continue;
                    }
                    catch (TownyException x) {
                        if (!townyWorld.isUsingTowny()) {
                            continue;
                        }
                        if (!townyWorld.isExpl()) {
                            event.setCancelled(true);
                            return;
                        }
                        if (!townyWorld.isUsingPlotManagementWildRevert() || entity == null || !townyWorld.isProtectingExplosionEntity(entity) || TownyRegenAPI.hasProtectionRegenTask(new BlockLocation(block.getLocation())) || block.getType() == Material.TNT) {
                            continue;
                        }
                        final ProtectionRegenTask task2 = new ProtectionRegenTask(this.plugin, block);
                        task2.setTaskId(this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)task2, (TownySettings.getPlotManagementWildRegenDelay() + count) * 20L));
                        TownyRegenAPI.addProtectionRegenTask(task2);
                        event.setYield(0.0f);
                        block.getDrops().clear();
                    }
                }
            }
        }
        else {
            int count2 = 0;
            for (Block block : blocks) {
                if (TownySettings.getWarSiegeEnabled() && SiegeWarBlockUtil.isBlockNearAnActiveSiegeBanner(block)) {
                    TownyMessaging.sendDebugMsg("onEntityExplode: Canceled " + event.getEntity().getEntityId() + " from exploding near siege banner");
                    event.setCancelled(true);
                    return;
                }
                final Coord coord = Coord.parseCoord(block.getLocation());
                ++count2;
                TownBlock townBlock2 = null;
                try {
                    townBlock2 = townyWorld.getTownBlock(coord);
                    if (townyWorld.isUsingTowny() && !townyWorld.isForceExpl() && (!townBlock2.getPermissions().explosion || (TownyAPI.getInstance().isWarTime() && TownySettings.isAllowWarBlockGriefing() && !townBlock2.getTown().hasNation() && !townBlock2.getTown().isBANG())) && event.getEntity() != null) {
                        TownyMessaging.sendDebugMsg("onEntityExplode: Canceled " + event.getEntity().getEntityId() + " from exploding within " + coord.toString() + ".");
                        event.setCancelled(true);
                        return;
                    }
                    continue;
                }
                catch (TownyException x) {
                    if (!townyWorld.isUsingTowny()) {
                        continue;
                    }
                    if (!townyWorld.isExpl()) {
                        event.setCancelled(true);
                        return;
                    }
                    if (!townyWorld.isUsingPlotManagementWildRevert() || entity == null || !townyWorld.isProtectingExplosionEntity(entity)) {
                        continue;
                    }
                    if (block.getType().equals((Object)Material.PISTON_EXTENSION)) {
                    	final BlockState blockState = block.getState();
                        final PistonExtensionMaterial blockData = (PistonExtensionMaterial)blockState.getData();
                        final Block baseBlock = block.getRelative(blockData.getAttachedFace());
                        final BlockState baseState = baseBlock.getState();
                        final PistonBaseMaterial baseData = (PistonBaseMaterial)baseState.getData();
                        block = baseBlock;
                        if (!TownyRegenAPI.hasProtectionRegenTask(new BlockLocation(block.getLocation())) && block.getType() != Material.TNT) {
                            final ProtectionRegenTask task3 = new ProtectionRegenTask(this.plugin, block);
                            task3.setTaskId(this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)task3, (TownySettings.getPlotManagementWildRegenDelay() + count2) * 20L));
                            TownyRegenAPI.addProtectionRegenTask(task3);
                            event.setYield(0.0f);
                            block.getDrops().clear();
                        }
                        baseData.setPowered(false);
                        baseState.setData((MaterialData)baseData);
                        baseState.update();
                    }
                    else {
                        if (TownyRegenAPI.hasProtectionRegenTask(new BlockLocation(block.getLocation())) || block.getType() == Material.TNT) {
                            continue;
                        }
                        final ProtectionRegenTask task2 = new ProtectionRegenTask(this.plugin, block);
                        task2.setTaskId(this.plugin.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this.plugin, (Runnable)task2, (TownySettings.getPlotManagementWildRegenDelay() + count2) * 20L));
                        TownyRegenAPI.addProtectionRegenTask(task2);
                        event.setYield(0.0f);
                        block.getDrops().clear();
                        if (!(block.getState().getData() instanceof Attachable) && !(block.getState().getData() instanceof Sign) && !(block.getState().getData() instanceof PressurePlate) && !(block.getState() instanceof ShulkerBox)) {
                            continue;
                        }
                        block.setType(Material.AIR);
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityCombustByEntityEvent(final EntityCombustByEntityEvent event) throws NotRegisteredException {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        final Entity combuster = event.getCombuster();
        final Entity defender = event.getEntity();
        if (combuster instanceof Projectile) {
            final Object source = ((Projectile)combuster).getShooter();
            if (!(source instanceof LivingEntity)) {
                return;
            }
            final LivingEntity attacker = (LivingEntity)source;
            if (attacker != null && !TownyAPI.getInstance().isWarTime() && CombatUtil.preventDamageCall(this.plugin, (Entity)attacker, defender)) {
                combuster.remove();
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingBreak(final HangingBreakEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        TownyWorld townyWorld = null;
        String worldName = null;
        final Entity hanging = (Entity)event.getEntity();
        try {
            worldName = hanging.getWorld().getName();
            townyWorld = TownyUniverse.getInstance().getDataSource().getWorld(worldName);
            if (!townyWorld.isUsingTowny()) {
                return;
            }
        }
        catch (NotRegisteredException e1) {
            return;
        }
        if (event instanceof HangingBreakByEntityEvent) {
            final HangingBreakByEntityEvent evt = (HangingBreakByEntityEvent)event;
            Object remover = evt.getRemover();
            if (remover instanceof Projectile) {
                remover = ((Projectile)remover).getShooter();
            }
            if (remover instanceof Player) {
                final Player player = (Player)remover;
                final boolean bDestroy = PlayerCacheUtil.getCachePermission(player, hanging.getLocation(), Material.PAINTING, TownyPermission.ActionType.DESTROY);
                if (bDestroy) {
                    return;
                }
                final PlayerCache cache = this.plugin.getCache(player);
                event.setCancelled(true);
                if (cache.hasBlockErrMsg()) {
                    TownyMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
                }
            }
            else if (!this.locationCanExplode(townyWorld, hanging.getLocation())) {
                event.setCancelled(true);
            }
            else {
                final TownBlock tb = TownyAPI.getInstance().getTownBlock(hanging.getLocation());
                if (tb == null && townyWorld.isExpl() && townyWorld.isUsingPlotManagementWildRevert() && remover != null && townyWorld.isProtectingExplosionEntity((Entity)remover)) {
                    event.setCancelled(true);
                }
            }
        }
        else {
            switch (event.getCause()) {
                case EXPLOSION: {
                    if (!this.locationCanExplode(townyWorld, event.getEntity().getLocation())) {
                        event.setCancelled(true);
                        break;
                    }
                    break;
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHangingPlace(final HangingPlaceEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        final Entity hanging = (Entity)event.getEntity();
        if (!TownyAPI.getInstance().isTownyWorld(hanging.getWorld())) {
            return;
        }
        final Player player = event.getPlayer();
        final boolean bBuild = PlayerCacheUtil.getCachePermission(player, hanging.getLocation(), Material.PAINTING, TownyPermission.ActionType.BUILD);
        event.setCancelled(!bBuild);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPigHitByLightning(final PigZapEvent event) {
        if (this.plugin.isError()) {
            event.setCancelled(true);
            return;
        }
        if (!TownyAPI.getInstance().isTownyWorld(event.getEntity().getWorld())) {
            return;
        }
        try {
            if (!this.locationCanExplode(TownyAPI.getInstance().getDataSource().getWorld(event.getEntity().getWorld().getName()), event.getEntity().getLocation())) {
                event.setCancelled(true);
            }
        }
        catch (NotRegisteredException ex) {}
    }
}
