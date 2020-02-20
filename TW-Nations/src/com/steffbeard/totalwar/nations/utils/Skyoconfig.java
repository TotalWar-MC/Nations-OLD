package com.steffbeard.totalwar.nations.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import org.bukkit.ChatColor;
import org.bukkit.util.Vector;
import org.bukkit.Bukkit;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONObject;
import org.bukkit.Location;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.configuration.ConfigurationSection;
import java.util.Map;
import com.google.common.primitives.Primitives;
import org.json.simple.parser.ParseException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.io.IOException;
import com.google.common.base.Joiner;
import java.lang.reflect.Field;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.List;
import java.io.File;

public class Skyoconfig {

    private static final transient String LINE_SEPARATOR;
    private transient File configFile;
    private transient List<String> header;
    
    protected Skyoconfig(final File configFile) {
        this(configFile, null);
    }
    
    protected Skyoconfig(final File configFile, final List<String> header) {
        this.configFile = configFile;
        this.header = header;
    }
    
    public final void load() throws InvalidConfigurationException {
        try {
            final YamlConfiguration config = YamlConfiguration.loadConfiguration(this.configFile);
            for (Class<?> clazz = this.getClass(); clazz != Skyoconfig.class; clazz = clazz.getSuperclass()) {
                for (final Field field : clazz.getFields()) {
                    this.loadField(field, this.getFieldName(field), config);
                }
            }
            this.saveConfig(config);
        }
        catch (Exception ex) {
            throw new InvalidConfigurationException((Throwable)ex);
        }
    }
    
    public final void save() throws InvalidConfigurationException {
        try {
            final YamlConfiguration config = YamlConfiguration.loadConfiguration(this.configFile);
            for (Class<?> clazz = this.getClass(); clazz != Skyoconfig.class; clazz = clazz.getSuperclass()) {
                for (final Field field : clazz.getFields()) {
                    this.saveField(field, this.getFieldName(field), config);
                }
            }
            this.saveConfig(config);
        }
        catch (Exception ex) {
            throw new InvalidConfigurationException((Throwable)ex);
        }
    }
    
    private String getFieldName(final Field field) {
        final ConfigOptions options = field.getAnnotation(ConfigOptions.class);
        if (options == null) {
            return field.getName().replace('_', '.');
        }
        final String name = options.name();
        if (name.equals("")) {
            return field.getName().replace('_', '.');
        }
        return name;
    }
    
    private boolean ignoreField(final Field field) {
        final ConfigOptions options = field.getAnnotation(ConfigOptions.class);
        return options != null && options.ignore();
    }
    
    private void saveConfig(final YamlConfiguration config) throws IOException {
        if (this.header != null && this.header.size() > 0) {
            config.options().header(Joiner.on(Skyoconfig.LINE_SEPARATOR).join((Iterable<?>)this.header));
        }
        config.save(this.configFile);
    }
    
    private void loadField(final Field field, final String name, final YamlConfiguration config) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ParseException, InstantiationException {
        if (Modifier.isTransient(field.getModifiers()) || this.ignoreField(field)) {
            return;
        }
        final Object configValue = config.get(this.getFieldName(field));
        if (configValue == null) {
            this.saveField(field, name, config);
        }
        else {
            field.set(this, this.deserializeObject(field.getType(), configValue));
        }
    }
    
    private void saveField(final Field field, final String name, final YamlConfiguration config) throws IllegalAccessException {
        if (Modifier.isTransient(field.getModifiers()) || this.ignoreField(field)) {
            return;
        }
        config.set(name, this.serializeObject(field.get(this), config));
    }
    
    private Object deserializeObject(final Class<?> clazz, final Object object) throws ParseException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        if (clazz.isPrimitive()) {
            return Primitives.wrap((Class<?>)clazz).getMethod("valueOf", String.class).invoke(this, object.toString());
        }
        if (Primitives.isWrapperType((Class<?>)clazz)) {
            return clazz.getMethod("valueOf", String.class).invoke(this, object.toString());
        }
        if (Map.class.isAssignableFrom(clazz) || object instanceof Map) {
            final ConfigurationSection section = (ConfigurationSection)object;
            final Map<Object, Object> deserializedMap = new HashMap<Object, Object>();
            for (final String key : section.getKeys(false)) {
                final Object value = section.get(key);
                deserializedMap.put(key, this.deserializeObject(value.getClass(), value));
            }
            final Object map = clazz.newInstance();
            clazz.getMethod("putAll", Map.class).invoke(map, deserializedMap);
            return map;
        }
        if (List.class.isAssignableFrom(clazz) || object instanceof List) {
            final List<Object> result = new ArrayList<Object>();
            for (final Object value2 : (List<?>)object) {
                result.add(this.deserializeObject(value2.getClass(), value2));
            }
            return result;
        }
        if (Location.class.isAssignableFrom(clazz) || object instanceof Location) {
            final JSONObject jsonObject = (JSONObject)new JSONParser().parse(object.toString());
            return new Location(Bukkit.getWorld(jsonObject.get((Object)"world").toString()), Double.parseDouble(jsonObject.get((Object)"x").toString()), Double.parseDouble(jsonObject.get((Object)"y").toString()), Double.parseDouble(jsonObject.get((Object)"z").toString()), Float.parseFloat(jsonObject.get((Object)"yaw").toString()), Float.parseFloat(jsonObject.get((Object)"pitch").toString()));
        }
        if (Vector.class.isAssignableFrom(clazz) || object instanceof Vector) {
            final JSONObject jsonObject = (JSONObject)new JSONParser().parse(object.toString());
            return new Vector(Double.parseDouble(jsonObject.get((Object)"x").toString()), Double.parseDouble(jsonObject.get((Object)"y").toString()), Double.parseDouble(jsonObject.get((Object)"z").toString()));
        }
        return ChatColor.translateAlternateColorCodes('&', object.toString());
    }
    
    @SuppressWarnings("unchecked")
	private Object serializeObject(final Object object, final YamlConfiguration config) {
        if (object instanceof String) {
            return object.toString().replace('§', '&');
        }
        if (object instanceof Enum) {
            return ((Enum<?>)object).name();
        }
        if (object instanceof Map) {
            final ConfigurationSection section = config.createSection("temp");
            for (final Map.Entry<?, ?> entry : ((Map <Object, Object> )object).entrySet()) {
                section.set(entry.getKey().toString(), this.serializeObject(entry.getValue(), config));
            }
            config.set("temp", (Object)null);
            return section;
        }
        if (object instanceof List) {
            final List<Object> result = new ArrayList<Object>();
            for (final Object value : (List<?>)object) {
                result.add(this.serializeObject(value, config));
            }
            return result;
        }
        if (object instanceof Location) {
            final Location location = (Location)object;
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put((Object)"world", (Object)location.getWorld().getName());
            jsonObject.put((Object)"x", (Object)location.getX());
            jsonObject.put((Object)"y", (Object)location.getY());
            jsonObject.put((Object)"z", (Object)location.getZ());
            jsonObject.put((Object)"yaw", (Object)location.getYaw());
            jsonObject.put((Object)"pitch", (Object)location.getPitch());
            return jsonObject.toJSONString();
        }
        if (object instanceof Vector) {
            final Vector vector = (Vector)object;
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put((Object)"x", (Object)vector.getX());
            jsonObject.put((Object)"y", (Object)vector.getY());
            jsonObject.put((Object)"z", (Object)vector.getZ());
            return jsonObject.toJSONString();
        }
        return object;
    }
    
    public final List<String> getHeader() {
        return this.header;
    }
    
    public final File getFile() {
        return this.configFile;
    }
    
    public final void setHeader(final List<String> header) {
        this.header = header;
    }
    
    public final void setFile(final File configFile) {
        this.configFile = configFile;
    }
    
    static {
        LINE_SEPARATOR = System.lineSeparator();
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.FIELD })
    protected @interface ConfigOptions {
        String name() default "";
        
        boolean ignore() default false;
    }
}
