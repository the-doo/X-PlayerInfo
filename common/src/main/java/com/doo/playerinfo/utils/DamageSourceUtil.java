package com.doo.playerinfo.utils;

import com.doo.playerinfo.attributes.ExtractAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class DamageSourceUtil {

    private DamageSourceUtil() {}

    public static float reductionFromArmor(DamageSource currentDamageSource, float armor) {
        if (currentDamageSource == null || currentDamageSource.getEntity() == null) {
            return armor;
        }

        Entity attacker = currentDamageSource.getDirectEntity();
        if ((currentDamageSource.is(DamageTypes.ARROW) || currentDamageSource.is(DamageTypes.TRIDENT) || currentDamageSource.is(DamageTypes.MOB_PROJECTILE))) {
            attacker = currentDamageSource.getEntity();
        }

        if (attacker instanceof LivingEntity e && e.getAttributes().hasAttribute(ExtractAttributes.ARMOR_PENETRATION)) {
            float v = (float) e.getAttributeValue(ExtractAttributes.ARMOR_PENETRATION);
            return v < 0 ? armor : armor * (1 - v);
        }

        return armor;
    }
}
