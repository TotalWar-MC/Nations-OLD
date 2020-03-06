package com.steffbeard.totalwar.nations.utils;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class BlockSerialization {
    public static String toBase64(Set<SBlock> setBlocks) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            // Save every element in the list
            for (SBlock block : setBlocks) {
                dataOutput.writeObject(block);
            }           
            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save blocks.", e);
        }        
    }
    
    public static Set<SBlock> fromBase64(String data, int size) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Set<SBlock> setBlocks = new HashSet<SBlock>();
            // Read the serialized list
            for (int i = 0; i < size; i++) {
            	setBlocks.add((SBlock)dataInput.readObject());
            }
            dataInput.close();
            return setBlocks;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}