package com.steffbeard.totalwar.nations.tasks;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.material.PistonExtensionMaterial;

import com.steffbeard.totalwar.nations.Main;
import com.steffbeard.totalwar.nations.utils.BlockUtils;
import com.steffbeard.totalwar.nations.utils.InventoryUtil;
import com.steffbeard.totalwar.nations.utils.SBlock;

import me.mrCookieSlime.CSCoreLibPlugin.general.World.CustomSkull;

public class DelayedRegenTask implements Runnable {
	
	private SBlock sb;
	private Material mat;
	private Block block;
	private Main plugin;
	
	public DelayedRegenTask(SBlock sb){
		Location l = new Location(Bukkit.getServer().getWorld(sb.world), sb.x, sb.y, sb.z);
		this.sb = sb;
		this.block = l.getBlock();
		this.mat = Material.valueOf(sb.mat);
	}
	
	public DelayedRegenTask(Block block){
		this.sb = plugin.getInstance().getStoredSBlock(block);
		Location l = new Location(Bukkit.getServer().getWorld(sb.world), sb.x, sb.y, sb.z);
		this.block = l.getBlock();
		this.mat = Material.valueOf(sb.mat);
	}
	
    @Override
	@SuppressWarnings("deprecation")
	public void run() {
    	//if(sb.type.equals("block")){
	    	block.setTypeIdAndData(mat.getId(), sb.data, true);
			BlockState blockState = block.getState();
			if(blockState.getData() instanceof Door){
				Door door = (Door) blockState.getData(); 
		    	Location topLoc = new Location(Bukkit.getServer().getWorld(sb.doorTopWorld),sb.doorTopX,sb.doorTopY,sb.doorTopZ);
			   	Location botLoc = new Location(Bukkit.getServer().getWorld(sb.doorBotWorld),sb.doorBotX,sb.doorBotY,sb.doorBotZ);
		    	Block topHalf = topLoc.getBlock();
			   	Block bottomHalf = botLoc.getBlock();
		    	door.setTopHalf(true);
				topHalf.setTypeIdAndData(mat.getId(), sb.doorTopData, false);
				door.setTopHalf(false);
				bottomHalf.setTypeIdAndData(mat.getId(), sb.doorBotData, false);
			}
			if(blockState instanceof Sign){
				if(!sb.signLines.isEmpty() && !sb.signLines.equals(null)){
					Sign sign = (Sign) blockState;
					int i = 0;
					for (String line : sb.signLines){
						if(!line.equals(null) || !line.equals(""))
							sign.setLine(i++, line);
					}
					sign.update(true);
				}
			}
			if(blockState instanceof CreatureSpawner){
				CreatureSpawner spawner = (CreatureSpawner) blockState;
				spawner.setCreatureTypeByName(sb.entityType);
				spawner.setDelay(sb.delay);
				
			}
			if(blockState instanceof Skull){
				Skull skull = (Skull) blockState;
				skull.setSkullType(SkullType.valueOf(sb.skullType));
				if(!sb.skullOwner.equals("") && !sb.skullOwner.equals(null) && sb.skullType.equalsIgnoreCase("player")){
					skull.setOwner(sb.skullOwner);
					if((skull.getOwner().toString().toLowerCase()).equals("cscorelib") && !sb.customTexture.equals(null) && !sb.customTexture.equals("")){
						try {
							CustomSkull.setSkull(blockState.getBlock(), sb.customTexture);
						} catch (Exception e) {				
							e.printStackTrace();
						}
					}else{
						skull.update();
					}
				}
			}
			if (blockState.getData() instanceof PistonExtensionMaterial) {
				PistonExtensionMaterial extension = (PistonExtensionMaterial) blockState.getData();
				extension.setData(sb.extensionByte);
				extension.setFacingDirection(BlockFace.valueOf(sb.face));
				extension.setSticky(sb.isSticky);
				Block piston = (blockState.getBlock()).getRelative(extension.getAttachedFace());
				piston.setTypeIdAndData(mat.getId(), sb.pistonByte, false);
			}
			if(blockState instanceof InventoryHolder) {
				Inventory container = ((InventoryHolder) blockState).getInventory();
				(blockState.getBlock()).setData(sb.inventoryData);
				ItemStack[] items;
				try {
					items = InventoryUtil.stacksFromBase64(sb.inventory);
					if(!items.equals(null)){
						container.setContents(items);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
			if(blockState.getType().equals(Material.DEAD_BUSH)){
				Block blockBelow = block.getRelative(BlockFace.DOWN);
				blockBelow.setType(Material.SAND, true);
			}
			if(BlockUtils.isCrops(blockState.getType())){
				Block blockBelow = block.getRelative(BlockFace.DOWN);
				blockBelow.setType(Material.SOIL, true);
			}
    	//}
    	/*if(sb.type.equals("itemframe")){
    		Location l = new Location((Bukkit.getServer().getWorld(sb.world)), sb.x, sb.y, sb.z);
    		ItemFrame i = (ItemFrame) (Bukkit.getServer().getWorld(sb.world)).spawn(l, ItemFrame.class);
    		i.setFacingDirection(BlockFace.valueOf(sb.face));
    		ItemStack itemInFrame;
			try {
				itemInFrame = InventoryUtil.stackFromBase64(sb.itemInFrame);
				i.setItem(itemInFrame);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		i.setRotation(Rotation.valueOf(sb.rotation));   		
    	}*/
	}
}