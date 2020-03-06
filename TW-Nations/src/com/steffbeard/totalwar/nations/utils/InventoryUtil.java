package com.steffbeard.totalwar.nations.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Matthew on 03/04/2015.
 */
public class InventoryUtil {
	public static String toBase64(ItemStack item) {
        return toBase64(item);
    }
    /**
     * Serializes an inventory to Base64
     *
     * @param inventory The inventory to serialize
     * @return The serialized string
     */
    public static String toBase64(Inventory inventory) {
        return toBase64(inventory.getContents());
    }

    /**
     * Serializes an ItemStack array to Base64
     *
     * @param contents The array to serialize
     * @return The serialized string
     */
    public static String toBase64(ItemStack[] contents) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(contents.length);

            for (ItemStack stack : contents) {
                dataOutput.writeObject(stack);
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    /**
     * Deserializes an inventory from Base64
     *
     * @param data The string data
     * @return The deserialized Inventory
     * @throws IOException If there was an error reading the data
     */
    public static Inventory inventoryFromBase64(String data) throws IOException {
        ItemStack[] stacks = stacksFromBase64(data);
        Inventory inventory = Bukkit.createInventory(null, (int) Math.ceil(stacks.length / 9D) * 9);

        for (int i = 0 ; i < stacks.length ; i++) {
            inventory.setItem(i, stacks[i]);
        }

        return inventory;
    }

    /**
     * Deserializes an ItemStack array from Base64
     *
     * @param data The string data
     * @return The deserialized array
     * @throws IOException If there was an error reading the data
     */
    public static ItemStack[] stacksFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] stacks = new ItemStack[dataInput.readInt()];

            for (int i = 0 ; i < stacks.length ; i++) {
                stacks[i] = (ItemStack) dataInput.readObject();
            }
            dataInput.close();
            return stacks;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
    
    public static ItemStack stackFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack stack = (ItemStack) dataInput.readObject();
            dataInput.close();
            return stack;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}
