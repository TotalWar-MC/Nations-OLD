package com.steffbeard.totalwar.nations.misc.trails;

import java.io.IOException;
import java.io.InputStream;
import org.bukkit.util.io.BukkitObjectInputStream;
import java.io.ByteArrayInputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import java.io.OutputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.ByteArrayOutputStream;

public class SerializeLocation
{
    public static String toBase64(final WrappedLocation location) {
        try {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream((OutputStream)outputStream);
            dataOutput.writeObject((Object)location);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        }
        catch (Exception e) {
            throw new IllegalStateException("Unable to save locations.", e);
        }
    }
    
    public static WrappedLocation fromBase64(final String locationSerial) throws IOException {
        try {
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(locationSerial));
            final BukkitObjectInputStream dataInput = new BukkitObjectInputStream((InputStream)inputStream);
            final WrappedLocation location = (WrappedLocation)dataInput.readObject();
            dataInput.close();
            return location;
        }
        catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}
