package com.doo.playerinfo.utils;

import com.doo.playerinfo.attributes.ExtractAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class DamageSourceUtil {

    private DamageSourceUtil() {
    }

    public static float reductionFromArmor(DamageSource source, float armor) {
        LivingEntity attacker = get(source);
        if (attacker == null) {
            return armor;
        }

        if (attacker.getAttributes().hasAttribute(ExtractAttributes.ARMOR_PENETRATION)) {
            float v = (float) attacker.getAttributeValue(ExtractAttributes.ARMOR_PENETRATION);
            return v < 0 ? armor : armor * (1 - v);
        }

        return armor;
    }

    private static LivingEntity get(DamageSource source) {
        if (source == null || source.getEntity() == null) {
            return null;
        }

        Entity attacker = source.getDirectEntity();
        if ((source.is(DamageTypes.ARROW) || source.is(DamageTypes.TRIDENT) || source.is(DamageTypes.MOB_PROJECTILE))) {
            attacker = source.getEntity();
        }

        return attacker instanceof LivingEntity living ? living : null;
    }

    public static float additionDamage(DamageSource source, float amount, float maxHealth) {
        LivingEntity attacker = get(source);
        if (attacker == null) {
            return amount;
        }

        AttributeMap attributes = attacker.getAttributes();
        // DAMAGE_PERCENTAGE_BONUS
        if (attributes.hasAttribute(ExtractAttributes.DAMAGE_PERCENTAGE_BONUS)) {
            float v = (float) attacker.getAttributeValue(ExtractAttributes.DAMAGE_PERCENTAGE_BONUS);
            amount = v <= 0 ? amount : amount + maxHealth * v;
        }

        // DAMAGE_PERCENTAGE_BONUS
        if (attributes.hasAttribute(ExtractAttributes.CRIT_RATE) && attacker.getRandom().nextDouble() < attributes.getValue(ExtractAttributes.CRIT_RATE)) {
            amount *= (1 + (float) attacker.getAttributeValue(ExtractAttributes.CRIT_DAMAGE));
        }
        return amount;
    }
}
