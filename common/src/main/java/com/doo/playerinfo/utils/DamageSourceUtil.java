package com.doo.playerinfo.utils;

import com.doo.playerinfo.attributes.ExtractAttributes;
import com.doo.playerinfo.interfaces.LivingEntityAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;

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

    public static void setHealingAddition(DamageSource source, float amount) {
        LivingEntity attacker = get(source);
        if (attacker == null) {
            return;
        }

        float healing = 0;
        float v;
        AttributeMap attributes = attacker.getAttributes();
        // DAMAGE_PERCENTAGE_HEALING
        if (attributes.hasAttribute(ExtractAttributes.DAMAGE_PERCENTAGE_HEALING)) {
            v = (float) attacker.getAttributeValue(ExtractAttributes.DAMAGE_PERCENTAGE_HEALING);
            healing = v <= 0 ? 0 : v * amount;
        }

        if (healing < 0.001) {
            return;
        }

        // log for player
        if (attacker instanceof Player) {
            LivingEntityAccessor.get(attacker).x_PlayerInfo$addDamageHealing(healing);
            return;
        }

        // ATTACK_HEALING
        if (attributes.hasAttribute(ExtractAttributes.ATTACK_HEALING)) {
            v = (float) attacker.getAttributeValue(ExtractAttributes.ATTACK_HEALING);
            healing += v <= 0 ? 0 : v;
        }

        attacker.heal(healing);
        LivingEntityAccessor.get(attacker).x_PlayerInfo$resetHealing();
    }

    public static void healingIfPlayerHasAttr(LivingEntity entity) {
        // Player ATTACK_HEALING
        float healing = 0;
        float v;
        AttributeMap attributes = entity.getAttributes();
        if (attributes.hasAttribute(ExtractAttributes.ATTACK_HEALING)) {
            v = (float) entity.getAttributeValue(ExtractAttributes.ATTACK_HEALING);
            healing = v <= 0 ? 0 : v;
        }
        LivingEntityAccessor accessor = LivingEntityAccessor.get(entity);
        if (healing >= 0.001) {
            accessor.x_PlayerInfo$addDamageHealing(healing);
        }

        accessor.x_PlayerInfo$healingPlayer();
        accessor.x_PlayerInfo$resetHealing();
    }
}
