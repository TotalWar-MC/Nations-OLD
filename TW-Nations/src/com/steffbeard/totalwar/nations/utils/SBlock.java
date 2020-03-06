package com.steffbeard.totalwar.nations.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.material.PistonExtensionMaterial;

import me.mrCookieSlime.CSCoreLibPlugin.general.World.CustomSkull;

@SuppressWarnings("deprecation")
public class SBlock implements Serializable{
	
	private static final long serialVersionUID = -5944092517430475805L;
	
	//public String type;
	public String world;
	public String mat;
	public UUID ent;
	public int x;
	public int y;
	public int z;
	public byte data;
	//Info for Storing Doors and their movements
	public String doorTopWorld;
	public String doorTopMat;
	public int doorTopX;
	public int doorTopY;
	public int doorTopZ;
	public byte doorTopData;
	public String doorBotWorld;
	public String doorBotMat;
	public int doorBotX;
	public int doorBotY;
	public int doorBotZ;
	public byte doorBotData;
	//Info for Storing Signs
	public List<String> signLines;
	//Info for Storing Spawners
	public String entityType;
	public int delay;
	//Info for Storing Skulls
	public String skullType;
	public String skullOwner = "";
	public String customTexture = "";
	//info for storing Pistons
	public String face;
	public boolean isSticky;
	public byte extensionByte;
	public byte pistonByte;
	//info for storing Inventories
	public byte inventoryData;
	public String inventory;/*
	//info for storing itemframes
	public String itemInFrame;
	public String rotation;
	//info for storing armorstands
	public Double bodyPoseX;
	public Double bodyPoseY;
	public Double bodyPoseZ;
	public Double headPoseX;
	public Double headPoseY;
	public Double headPoseZ;
	public Double leftArmPoseX;
	public Double leftArmPoseY;
	public Double leftArmPoseZ;
	public Double rightArmPoseX;
	public Double rightArmPoseY;
	public Double rightArmPoseZ;
	public Double leftLegPoseX;
	public Double leftLegPoseY;
	public Double leftLegPoseZ;
	public Double rightLegPoseX;
	public Double rightLegPoseY;
	public Double rightLegPoseZ;
	public String itemInMainHand;
	public String itemInOffHand;
	public String armor;*/
	
	
	public SBlock(Block block, Entity entity){
		//type = "block";
		world = block.getLocation().getWorld().getName().toString();		
		x = block.getLocation().getBlockX();
		y = block.getLocation().getBlockY();
		z = block.getLocation().getBlockZ();
		mat = block.getType().name().toString();
		data = block.getData();
		ent = entity.getUniqueId();
		if(block.getState().getData() instanceof Door){
			Door door = (Door) block.getState().getData();
			Block topHalf;
			Block bottomHalf;
			if (door.isTopHalf()) {
				topHalf = block.getState().getBlock();
				bottomHalf = block.getState().getBlock().getRelative(BlockFace.DOWN);
			} else {
				bottomHalf = block.getState().getBlock();
				topHalf = block.getState().getBlock().getRelative(BlockFace.UP);
			}
			doorTopWorld = topHalf.getLocation().getWorld().getName().toString();		
			doorTopX = topHalf.getLocation().getBlockX();
			doorTopY = topHalf.getLocation().getBlockY();
			doorTopZ = topHalf.getLocation().getBlockZ();
			doorTopMat = topHalf.getType().name().toString();
			doorTopData = topHalf.getData();
			doorBotWorld = bottomHalf.getLocation().getWorld().getName().toString();		
			doorBotX = bottomHalf.getLocation().getBlockX();
			doorBotY = bottomHalf.getLocation().getBlockY();
			doorBotZ = bottomHalf.getLocation().getBlockZ();
			doorBotMat = bottomHalf.getType().name().toString();
			doorBotData = bottomHalf.getData();			
		}
		if(block.getState() instanceof Sign){
			Sign sign = (Sign)(block.getState());
			signLines = new ArrayList<String>();
			if(!sign.getLines().equals(null) && !(sign.getLines().length <= 0)){
				for (String line : sign.getLines()){
					signLines.add(line);
				}
			}else{
				signLines = null;
			}			
		}
		if(block.getState() instanceof CreatureSpawner){
			CreatureSpawner spawner = (CreatureSpawner) block.getState();
			entityType = spawner.getCreatureTypeName();
			delay = spawner.getDelay();
		}
		if(block.getState() instanceof Skull){
			Skull skull = (Skull) block.getState();
			skullType = skull.getSkullType().name().toString();
			if(skull.hasOwner()){
				skullOwner = skull.getOwner().toString();
				if(skullOwner.toLowerCase().equals("cscorelib")){
					try {
						customTexture = CustomSkull.getTexture(block);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (block.getState().getData() instanceof PistonExtensionMaterial) {
			PistonExtensionMaterial extension = (PistonExtensionMaterial) block.getState().getData();
			face = extension.getFacing().name().toString();
			isSticky = extension.isSticky();
			extensionByte = extension.getData();
			Block piston = block.getRelative(extension.getAttachedFace());
			pistonByte = piston.getState().getData().getData();
		}
		if(block.getState() instanceof InventoryHolder) {
			inventoryData = block.getState().getData().getData();
			ItemStack[] inv = ((InventoryHolder) block.getState()).getInventory().getContents();
			inventory = InventoryUtil.toBase64(inv);
		}
	}
	
	public SBlock(Block block){
		//type = "block";
		world = block.getLocation().getWorld().getName().toString();		
		x = block.getLocation().getBlockX();
		y = block.getLocation().getBlockY();
		z = block.getLocation().getBlockZ();
		mat = block.getType().name().toString();
		data = block.getData();
		ent = null;
		if(block.getState().getData() instanceof Door){
			Door door = (Door) block.getState().getData();
			Block topHalf;
			Block bottomHalf;
			if (door.isTopHalf()) {
				topHalf = block.getState().getBlock();
				bottomHalf = block.getState().getBlock().getRelative(BlockFace.DOWN);
			} else {
				bottomHalf = block.getState().getBlock();
				topHalf = block.getState().getBlock().getRelative(BlockFace.UP);
			}
			doorTopWorld = topHalf.getLocation().getWorld().getName().toString();		
			doorTopX = topHalf.getLocation().getBlockX();
			doorTopY = topHalf.getLocation().getBlockY();
			doorTopZ = topHalf.getLocation().getBlockZ();
			doorTopMat = topHalf.getType().name().toString();
			doorTopData = topHalf.getData();
			doorBotWorld = bottomHalf.getLocation().getWorld().getName().toString();		
			doorBotX = bottomHalf.getLocation().getBlockX();
			doorBotY = bottomHalf.getLocation().getBlockY();
			doorBotZ = bottomHalf.getLocation().getBlockZ();
			doorBotMat = bottomHalf.getType().name().toString();
			doorBotData = bottomHalf.getData();			
		}
		if(block.getState() instanceof Sign){
			Sign sign = (Sign)(block.getState());
			signLines = new ArrayList<String>();
			if(!sign.getLines().equals(null) && !(sign.getLines().length <= 0)){
				for (String line : sign.getLines()){
					signLines.add(line);
				}
			}else{
				signLines = null;
			}			
		}
		if(block.getState() instanceof CreatureSpawner){
			CreatureSpawner spawner = (CreatureSpawner) block.getState();
			entityType = spawner.getCreatureTypeName();
			delay = spawner.getDelay();
		}
		if(block.getState() instanceof Skull){
			Skull skull = (Skull) block.getState();
			skullType = skull.getSkullType().name().toString();
			if(skull.hasOwner()){
				skullOwner = skull.getOwner().toString();
				if(skullOwner.toLowerCase().equals("cscorelib")){
					try {
						customTexture = CustomSkull.getTexture(block);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (block.getState().getData() instanceof PistonExtensionMaterial) {
			PistonExtensionMaterial extension = (PistonExtensionMaterial) block.getState().getData();
			face = extension.getFacing().name().toString();
			isSticky = extension.isSticky();
			extensionByte = extension.getData();
			Block piston = block.getRelative(extension.getAttachedFace());
			pistonByte = piston.getState().getData().getData();
		}
		if(block.getState() instanceof InventoryHolder) {
			inventoryData = block.getState().getData().getData();
			ItemStack[] inv = ((InventoryHolder) block.getState()).getInventory().getContents();
			inventory = InventoryUtil.toBase64(inv);
		}
	}
	
	public SBlock(Location loc){
		Block block = loc.getBlock();
		world = block.getLocation().getWorld().getName().toString();		
		x = block.getLocation().getBlockX();
		y = block.getLocation().getBlockY();
		z = block.getLocation().getBlockZ();
		mat = block.getType().name().toString();
		data = block.getData();
		ent = null;
		if(block.getState().getData() instanceof Door){
			Door door = (Door) block.getState().getData();
			Block topHalf;
			Block bottomHalf;
			if (door.isTopHalf()) {
				topHalf = block.getState().getBlock();
				bottomHalf = block.getState().getBlock().getRelative(BlockFace.DOWN);
			} else {
				bottomHalf = block.getState().getBlock();
				topHalf = block.getState().getBlock().getRelative(BlockFace.UP);
			}
			doorTopWorld = topHalf.getLocation().getWorld().getName().toString();		
			doorTopX = topHalf.getLocation().getBlockX();
			doorTopY = topHalf.getLocation().getBlockY();
			doorTopZ = topHalf.getLocation().getBlockZ();
			doorTopMat = topHalf.getType().name().toString();
			doorTopData = topHalf.getData();
			doorBotWorld = bottomHalf.getLocation().getWorld().getName().toString();		
			doorBotX = bottomHalf.getLocation().getBlockX();
			doorBotY = bottomHalf.getLocation().getBlockY();
			doorBotZ = bottomHalf.getLocation().getBlockZ();
			doorBotMat = bottomHalf.getType().name().toString();
			doorBotData = bottomHalf.getData();			
		}
		if(block.getState() instanceof Sign){
			Sign sign = (Sign)(block.getState());
			signLines = new ArrayList<String>();
			if(!sign.getLines().equals(null) && !(sign.getLines().length <= 0)){
				for (String line : sign.getLines()){
					signLines.add(line);
				}
			}else{
				signLines = null;
			}			
		}
		if(block.getState() instanceof CreatureSpawner){
			CreatureSpawner spawner = (CreatureSpawner) block.getState();
			entityType = spawner.getCreatureTypeName();
			delay = spawner.getDelay();
		}
		if(block.getState() instanceof Skull){
			Skull skull = (Skull) block.getState();
			skullType = skull.getSkullType().name().toString();
			if(skull.hasOwner()){
				skullOwner = skull.getOwner().toString();
				if(skullOwner.toLowerCase().equals("cscorelib")){
					try {
						customTexture = CustomSkull.getTexture(block);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (block.getState().getData() instanceof PistonExtensionMaterial) {
			PistonExtensionMaterial extension = (PistonExtensionMaterial) block.getState().getData();
			face = extension.getFacing().name().toString();
			isSticky = extension.isSticky();
			extensionByte = extension.getData();
			Block piston = block.getRelative(extension.getAttachedFace());
			pistonByte = piston.getState().getData().getData();
		}
		if(block.getState() instanceof InventoryHolder) {
			inventoryData = block.getState().getData().getData();
			ItemStack[] inv = ((InventoryHolder) block.getState()).getInventory().getContents();
			inventory = InventoryUtil.toBase64(inv);
		}
	}
	
	public SBlock(Location loc, Entity entity){
		Block block = loc.getBlock();
		world = block.getLocation().getWorld().getName().toString();		
		x = block.getLocation().getBlockX();
		y = block.getLocation().getBlockY();
		z = block.getLocation().getBlockZ();
		mat = block.getType().name().toString();
		data = block.getData();
		ent = entity.getUniqueId();
		if(block.getState().getData() instanceof Door){
			Door door = (Door) block.getState().getData();
			Block topHalf;
			Block bottomHalf;
			if (door.isTopHalf()) {
				topHalf = block.getState().getBlock();
				bottomHalf = block.getState().getBlock().getRelative(BlockFace.DOWN);
			} else {
				bottomHalf = block.getState().getBlock();
				topHalf = block.getState().getBlock().getRelative(BlockFace.UP);
			}
			doorTopWorld = topHalf.getLocation().getWorld().getName().toString();		
			doorTopX = topHalf.getLocation().getBlockX();
			doorTopY = topHalf.getLocation().getBlockY();
			doorTopZ = topHalf.getLocation().getBlockZ();
			doorTopMat = topHalf.getType().name().toString();
			doorTopData = topHalf.getData();
			doorBotWorld = bottomHalf.getLocation().getWorld().getName().toString();		
			doorBotX = bottomHalf.getLocation().getBlockX();
			doorBotY = bottomHalf.getLocation().getBlockY();
			doorBotZ = bottomHalf.getLocation().getBlockZ();
			doorBotMat = bottomHalf.getType().name().toString();
			doorBotData = bottomHalf.getData();			
		}
		if(block.getState() instanceof Sign){
			Sign sign = (Sign)(block.getState());
			signLines = new ArrayList<String>();
			if(!sign.getLines().equals(null) && !(sign.getLines().length <= 0)){
				for (String line : sign.getLines()){
					signLines.add(line);
				}
			}else{
				signLines = null;
			}			
		}
		if(block.getState() instanceof CreatureSpawner){
			CreatureSpawner spawner = (CreatureSpawner) block.getState();
			entityType = spawner.getCreatureTypeName();
			delay = spawner.getDelay();
		}
		if(block.getState() instanceof Skull){
			Skull skull = (Skull) block.getState();
			skullType = skull.getSkullType().name().toString();
			if(skull.hasOwner()){
				skullOwner = skull.getOwner().toString();
				if(skullOwner.toLowerCase().equals("cscorelib")){
					try {
						customTexture = CustomSkull.getTexture(block);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		if (block.getState().getData() instanceof PistonExtensionMaterial) {
			PistonExtensionMaterial extension = (PistonExtensionMaterial) block.getState().getData();
			face = extension.getFacing().name().toString();
			isSticky = extension.isSticky();
			extensionByte = extension.getData();
			Block piston = block.getRelative(extension.getAttachedFace());
			pistonByte = piston.getState().getData().getData();
		}
		if(block.getState() instanceof InventoryHolder) {
			inventoryData = block.getState().getData().getData();
			ItemStack[] inv = ((InventoryHolder) block.getState()).getInventory().getContents();
			inventory = InventoryUtil.toBase64(inv);
		}
	}
	
	public Block getBlock(){
		Location l = new Location(Bukkit.getServer().getWorld(this.world),this.x,this.y,this.z);
		return l.getBlock();
	}
	  
	public Location getLocation(){
		return new Location(Bukkit.getServer().getWorld(this.world),this.x,this.y,this.z);
	}
	
	public Material getType(){
		return Material.valueOf(this.mat);
	}
	
	public Entity getEntity(){
		for (World world : Bukkit.getWorlds()) {
			for (Entity entity : world.getEntities()) {
				if (entity.getUniqueId().equals(ent)){
					return entity;
	            }
			}
	    }
	    return null;
	}
	
	public double getX(){
		return this.x;
	}
	
	public double getY(){
		return this.y;
	}
	
	public double getZ(){
		return this.z;
	}
	
	public byte getData(){
		return this.data;
	}
	
	public BlockState getState(){
		Location l = new Location(Bukkit.getServer().getWorld(this.world),this.x,this.y,this.z);
		return l.getBlock().getState();
	}
	
	/*public SBlock(ItemFrame e){
		type = "itemframe";
		ent = null;
		world = e.getLocation().getWorld().getName().toString();
		x = e.getLocation().getBlockX();
		y = e.getLocation().getBlockY();
		z = e.getLocation().getBlockZ();
		itemInFrame = InventoryUtil.toBase64(e.getItem());
		rotation = e.getRotation().name().toString();
		face = e.getFacing().name().toString();
	}
	
	public SBlock(ItemFrame e, Entity entity){
		type = "itemframe";
		ent = entity.getUniqueId();
		world = e.getLocation().getWorld().getName().toString();
		x = e.getLocation().getBlockX();
		y = e.getLocation().getBlockY();
		z = e.getLocation().getBlockZ();
		itemInFrame = InventoryUtil.toBase64(e.getItem());
		rotation = e.getRotation().name().toString();
		face = e.getFacing().name().toString();
	}
	
	public SBlock(ArmorStand e){
		type = "armorstand";
		ent = null;
		world = e.getLocation().getWorld().getName().toString();
		x = e.getLocation().getBlockX();
		y = e.getLocation().getBlockY();
		z = e.getLocation().getBlockZ();
		bodyPoseX = e.getBodyPose().getX();
		bodyPoseY = e.getBodyPose().getY();
		bodyPoseZ = e.getBodyPose().getZ();
		headPoseX = e.getHeadPose().getX();
		headPoseY = e.getHeadPose().getY();
		headPoseZ = e.getHeadPose().getZ();
		leftArmPoseX = e.getLeftArmPose().getX();
		leftArmPoseY = e.getLeftArmPose().getY();
		leftArmPoseZ = e.getLeftArmPose().getZ();
		rightArmPoseX = e.getRightArmPose().getX();
		rightArmPoseY = e.getRightArmPose().getY();
		rightArmPoseZ = e.getRightArmPose().getZ();
		leftLegPoseX = e.getLeftLegPose().getX();
		leftLegPoseY = e.getLeftLegPose().getY();
		leftLegPoseZ = e.getLeftLegPose().getZ();
		rightLegPoseX = e.getRightLegPose().getX();
		rightLegPoseY = e.getRightLegPose().getY();
		rightLegPoseZ = e.getRightLegPose().getZ();
		EntityEquipment inv = e.getEquipment();
		armor = InventoryUtil.toBase64(inv.getArmorContents());
		itemInMainHand = InventoryUtil.toBase64(inv.getItemInMainHand());
		itemInOffHand = InventoryUtil.toBase64(inv.getItemInOffHand());
	}
	
	public SBlock(ArmorStand e, Entity entity){
		type = "armorstand";
		ent = entity.getUniqueId();
		world = e.getLocation().getWorld().getName().toString();
		x = e.getLocation().getBlockX();
		y = e.getLocation().getBlockY();
		z = e.getLocation().getBlockZ();
		bodyPoseX = e.getBodyPose().getX();
		bodyPoseY = e.getBodyPose().getY();
		bodyPoseZ = e.getBodyPose().getZ();
		headPoseX = e.getHeadPose().getX();
		headPoseY = e.getHeadPose().getY();
		headPoseZ = e.getHeadPose().getZ();
		leftArmPoseX = e.getLeftArmPose().getX();
		leftArmPoseY = e.getLeftArmPose().getY();
		leftArmPoseZ = e.getLeftArmPose().getZ();
		rightArmPoseX = e.getRightArmPose().getX();
		rightArmPoseY = e.getRightArmPose().getY();
		rightArmPoseZ = e.getRightArmPose().getZ();
		leftLegPoseX = e.getLeftLegPose().getX();
		leftLegPoseY = e.getLeftLegPose().getY();
		leftLegPoseZ = e.getLeftLegPose().getZ();
		rightLegPoseX = e.getRightLegPose().getX();
		rightLegPoseY = e.getRightLegPose().getY();
		rightLegPoseZ = e.getRightLegPose().getZ();
		EntityEquipment inv = e.getEquipment();
		armor = InventoryUtil.toBase64(inv.getArmorContents());
		itemInMainHand = InventoryUtil.toBase64(inv.getItemInMainHand());
		itemInOffHand = InventoryUtil.toBase64(inv.getItemInOffHand());
	}*/
}