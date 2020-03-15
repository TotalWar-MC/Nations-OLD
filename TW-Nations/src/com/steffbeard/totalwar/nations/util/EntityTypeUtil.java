package com.steffbeard.totalwar.nations.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.LivingEntity;

import com.steffbeard.totalwar.nations.config.Messages;

public class EntityTypeUtil {

	public static boolean isInstanceOfAny(List<Class<?>> classesOfWorldMobsToRemove2, Object obj) {

		for (Class<?> c : classesOfWorldMobsToRemove2)
			if (c.isInstance(obj))
				return true;
		return false;
	}

	public static List<Class<?>> parseLivingEntityClassNames(List<String> mobClassNames, String errorPrefix) {

		List<Class<?>> livingEntityClasses = new ArrayList<Class<?>>();
		for (String mobClassName : mobClassNames) {
			if (mobClassName.isEmpty())
				continue;

			try {
				Class<?> c = Class.forName("org.bukkit.entity." + mobClassName);
				if (JavaUtil.isSubInterface(LivingEntity.class, c))
					livingEntityClasses.add(c);
				else
					throw new Exception();
			} catch (ClassNotFoundException e) {
				Messages.sendErrorMsg(String.format("%s%s is not an acceptable class.", errorPrefix, mobClassName));
			} catch (Exception e) {
				Messages.sendErrorMsg(String.format("%s%s is not an acceptable living entity.", errorPrefix, mobClassName));
			}
		}
		return livingEntityClasses;
	}
}
