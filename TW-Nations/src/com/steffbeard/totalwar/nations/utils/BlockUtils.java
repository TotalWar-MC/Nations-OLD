package com.steffbeard.totalwar.nations.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class BlockUtils{
	
	public static ArrayList<?> setToList(Set<?> set){
		ArrayList list = new ArrayList();
		if(!set.equals(null) && !set.isEmpty()){
			for(Object sb : set)
	    	{
	    		list.add(sb);
	    	}
		} 	
    	return list;
	}
	
	public static Set<?> listToSet(List<?> list){
		HashSet set = new HashSet();
		if(!list.equals(null) && !list.isEmpty())
		{
	    	for(Object sb : list)
	    	{
	    		set.add(sb);
	    	}
		}
    	return set;
	}
	
	public static boolean isOtherAttachable(Material mat){
		switch(mat){
			case SNOW: return true;
			case LONG_GRASS: return true;
			case YELLOW_FLOWER: return true;
			case RED_ROSE: return true;
			case CARPET: return true;
			case DEAD_BUSH: return true;
			case RAILS: return true;
			case ACTIVATOR_RAIL: return true;
			case DETECTOR_RAIL: return true;
			case POWERED_RAIL: return true;
			case REDSTONE_WIRE: return true;
			case DOUBLE_PLANT: return true;
			case FLOWER_POT: return true;
			case DIODE_BLOCK_OFF: return true;
			case REDSTONE_COMPARATOR_OFF: return true;
			case DIODE_BLOCK_ON: return true;
			case REDSTONE_COMPARATOR_ON: return true;
			case WOOD_PLATE: return true;
			case STONE_PLATE: return true;
			case POTATO: return true;
			case CARROT: return true;
			case BEETROOT_BLOCK: return true;
			case MELON_STEM: return true;
			case PUMPKIN_STEM: return true;
			case CROPS: return true;
			case BROWN_MUSHROOM: return true;
			case RED_MUSHROOM: return true;
			case CHORUS_PLANT: return true;
			case CHORUS_FLOWER: return true;
			default: break;		
		}
		return false;
	}
	
	public static boolean isCrops(Material mat){
		switch(mat){
			case POTATO: return true;
			case CARROT: return true;
			case BEETROOT_BLOCK: return true;
			case MELON_STEM: return true;
			case PUMPKIN_STEM: return true;
			case CROPS: return true;
			default: break;
		}
		return false;
	}
	
	public static ArrayList<Entity> getNearbyEntities(Block block, double distance){
		double radius = distance;
		List<Entity> entities = block.getLocation().getWorld().getEntities();
		ArrayList<Entity> near = new ArrayList<Entity>();
		for(Entity e : entities) {
		    if(e.getLocation().distance(block.getLocation()) <= radius){
		    	near.add(e);
		    }
		}
		return near;
	}
	
	public static ArrayList<Block> getNearbyLiquids(Block block){
		ArrayList<Block> blocks = new ArrayList<Block>();
		for(int i = -2; i <= 2; i++){
			Location l = block.getLocation();
			l.add(i, i, i);
			Block check = l.getBlock();
			if(check.getType().equals(Material.WATER) || check.getType().equals(Material.STATIONARY_WATER)){
				blocks.add(check);
			}
		}
		return blocks;
	}
}