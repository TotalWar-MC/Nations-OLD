package com.palmergames.bukkit.towny.regen;

import org.bukkit.Material;
import com.palmergames.bukkit.towny.regen.block.BlockObject;
import org.bukkit.World;
import org.bukkit.block.Block;
import com.palmergames.bukkit.util.BukkitTools;
import com.palmergames.bukkit.towny.TownySettings;
import java.util.ArrayList;
import java.util.List;
import com.palmergames.bukkit.towny.object.TownBlock;

public class PlotBlockData
{
    private int defaultVersion;
    private String worldName;
    private TownBlock townBlock;
    private int x;
    private int z;
    private int size;
    private int height;
    private int version;
    private List<Integer> blockList;
    private int blockListRestored;
    
    public PlotBlockData(final TownBlock townBlock) {
        this.defaultVersion = 2;
        this.blockList = new ArrayList<Integer>();
        this.townBlock = townBlock;
        this.setX(townBlock.getX());
        this.setZ(townBlock.getZ());
        this.setSize(TownySettings.getTownBlockSize());
        this.worldName = townBlock.getWorld().getName();
        this.setVersion(this.defaultVersion);
        this.setHeight(townBlock.getWorldCoord().getBukkitWorld().getMaxHeight() - 1);
        this.blockListRestored = 0;
    }
    
    public void initialize() {
        final List<Integer> blocks = this.getBlockArr();
        if (blocks != null) {
            this.setBlockList(blocks);
            this.resetBlockListRestored();
        }
    }
    
    private List<Integer> getBlockArr() {
        final List<Integer> list = new ArrayList<Integer>();
        Block block = null;
        final World world = this.townBlock.getWorldCoord().getBukkitWorld();
        for (int z = 0; z < this.size; ++z) {
            for (int x = 0; x < this.size; ++x) {
                for (int y = this.height; y > 0; --y) {
                    block = world.getBlockAt(this.getX() * this.size + x, y, this.getZ() * this.size + z);
                    switch (this.defaultVersion) {
                        case 1:
                        case 2: {
                            list.add(BukkitTools.getTypeId(block));
                            list.add((int)BukkitTools.getData(block));
                            break;
                        }
                        default: {
                            list.add(BukkitTools.getTypeId(block));
                            break;
                        }
                    }
                }
            }
        }
        return list;
    }
    
    public boolean restoreNextBlock() {
        Block block = null;
        final int worldx = this.getX() * this.size;
        final int worldz = this.getZ() * this.size;
        final World world = this.townBlock.getWorldCoord().getBukkitWorld();
        if (!world.isChunkLoaded(BukkitTools.calcChunk(this.getX()), BukkitTools.calcChunk(this.getZ()))) {
            return true;
        }
        int scale = 0;
        switch (this.version) {
            case 1:
            case 2: {
                scale = 2;
                break;
            }
            default: {
                scale = 1;
                break;
            }
        }
        int reverse = (this.blockList.size() - this.blockListRestored) / scale;
        while (reverse > 0) {
            --reverse;
            final int y = this.height - reverse % this.height;
            final int x = reverse / this.height % this.size;
            final int z = reverse / this.height / this.size % this.size;
            block = world.getBlockAt(worldx + x, y, worldz + z);
            final int blockId = BukkitTools.getTypeId(block);
            final BlockObject storedData = this.getStoredBlockData(this.blockList.size() - 1 - this.blockListRestored);
            this.blockListRestored += scale;
            if (blockId != storedData.getTypeId() || BukkitTools.getData(block) != storedData.getData()) {
                final Material mat = BukkitTools.getMaterial(storedData.getTypeId());
                if (mat != null) {
                    if (mat == null || this.townBlock.getWorld().isPlotManagementIgnoreIds(mat.name(), storedData.getData())) {
                        BukkitTools.setTypeId(block, 0, false);
                        return true;
                    }
                }
                try {
                    switch (this.version) {
                        case 1:
                        case 2: {
                            BukkitTools.setTypeIdAndData(block, storedData.getTypeId(), storedData.getData(), false);
                            break;
                        }
                        default: {
                            BukkitTools.setTypeId(block, storedData.getTypeId(), false);
                            break;
                        }
                    }
                }
                catch (Exception ex) {}
                return true;
            }
        }
        this.resetBlockListRestored();
        return false;
    }
    
    private BlockObject getStoredBlockData(final int index) {
        switch (this.version) {
            case 1:
            case 2: {
                return new BlockObject(this.blockList.get(index - 1), (byte)(this.blockList.get(index) & 0xFF));
            }
            default: {
                return new BlockObject(this.blockList.get(index), (byte)0);
            }
        }
    }
    
    public int getX() {
        return this.x;
    }
    
    public void setX(final int x) {
        this.x = x;
    }
    
    public int getZ() {
        return this.z;
    }
    
    public void setZ(final int z) {
        this.z = z;
    }
    
    public int getSize() {
        return this.size;
    }
    
    public void setSize(final int size) {
        this.size = size;
    }
    
    public int getHeight() {
        return this.height;
    }
    
    public void setHeight(final int height) {
        this.height = height;
    }
    
    public String getWorldName() {
        return this.worldName;
    }
    
    public int getVersion() {
        return this.version;
    }
    
    public void setVersion(final int version) {
        this.version = version;
    }
    
    public List<Integer> getBlockList() {
        return this.blockList;
    }
    
    public void setBlockList(final List<Integer> blockList) {
        this.blockList = blockList;
    }
    
    public void resetBlockListRestored() {
        this.blockListRestored = 0;
    }
}
