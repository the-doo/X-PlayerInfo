package com.doo.playerinfo.utils;

import com.doo.playerinfo.interfaces.LivingEntityAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;

public class DamageSourceUtil {

    private static final ThreadLocal<Float> LOCAL = ThreadLocal.withInitial(() -> null);

    private DamageSourceUtil() {
    }

    public static float test(ServerPlayer player, DamageSource source, int damage) {
        DamageSourceUtil.startTest();
        LivingEntityAccessor.get(player).x_PlayerInfo$actuallyHurt(source, damage);
        return DamageSourceUtil.endTest();
    }

    public static void startTest() {
        LOCAL.set(0F);
    }

    public static void setDamage(float damage) {
        if (LOCAL.get() != null) {
            LOCAL.set(damage);
        }
    }

    public static boolean isTest() {
        return LOCAL.get() != null;
    }

    public static float endTest() {
        float f = LOCAL.get();
        LOCAL.remove();
        return f;
    }

    public static float reductionFromArmor(DamageSource source, float armor) {
        LivingEntity attacker = get(source);
        if (attacker == null) {
            return armor;
        }

        double v = ExtractAttributes.get(attacker.getAttributes(), ExtractAttributes.ARMOR_PENETRATION);
        return v > 0 ? (float) (armor * (1 - v)) : armor;
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
        double v = ExtractAttributes.get(attributes, ExtractAttributes.DAMAGE_PERCENTAGE_BONUS);
        amount = v <= 0 ? amount : (float) (amount + maxHealth * v);

        // DAMAGE_PERCENTAGE_BONUS
        v = ExtractAttributes.get(attributes, ExtractAttributes.CRIT_RATE);
        amount = attacker.getRandom().nextDouble() >= v ||
                (v = ExtractAttributes.get(attributes, ExtractAttributes.CRIT_DAMAGE)) != 0 ? amount : amount * (1 + (float) v);
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
        AttributeMap attributes = entity.getAttributes();
        float v = (float) ExtractAttributes.get(attributes, ExtractAttributes.ATTACK_HEALING);
        float healing = v <= 0 ? 0 : v;
        LivingEntityAccessor accessor = LivingEntityAccessor.get(entity);
        if (healing >= 0.001) {
            accessor.x_PlayerInfo$addDamageHealing(healing);
        }

        accessor.x_PlayerInfo$healingPlayer();
        accessor.x_PlayerInfo$resetHealing();
    }
}
