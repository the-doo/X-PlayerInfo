package com.doo.playerinfo.utils;

import com.doo.playerinfo.attributes.ExtractAttributes;
import com.doo.playerinfo.consts.Const;
import com.doo.playerinfo.core.InfoGroupItems;
import com.doo.playerinfo.core.InfoItemCollector;
import com.doo.playerinfo.interfaces.LivingEntityAccessor;
import com.google.common.collect.Lists;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;
import java.util.function.BiConsumer;

import static com.doo.playerinfo.consts.Const.MINECRAFT_NAME;

public abstract class InfoRegisters {

    private static DamageSource damageTest;

    private InfoRegisters() {
    }

    public static void initMinecraft() {
        InfoItemCollector.register(MINECRAFT_NAME, player -> {
            if (damageTest == null) {
                damageTest = player.level().damageSources().mobAttack(null);
            }

            List<InfoGroupItems> sorted = Lists.newArrayList();
            AttributeMap attributes = player.getAttributes();
            Abilities abilities = player.getAbilities();

            sorted.add(InfoGroupItems.group("base").attrMap(attributes)
                    .add(Const.HEALTH, player.getHealth())
                    .add(Attributes.MAX_HEALTH.getDescriptionId(), attributes.getValue(Attributes.MAX_HEALTH))
                    .addAttr(ExtractAttributes.HEALING_BONUS)
                    .add(Const.ABSORPTION_AMOUNT, player.getAbsorptionAmount())
                    .addAttr(ExtractAttributes.ABSORPTION_BONUS)
            );

            sorted.add(InfoGroupItems.group("xp").attrMap(attributes)
                    .add(Const.EXPERIENCE_LEVEL, player.experienceLevel)
                    .add(Const.TOTAL_EXPERIENCE, player.totalExperience)
                    .add(Const.EXPERIENCE_PROGRESS, player.experienceProgress)
                    .addAttr(ExtractAttributes.XP_BONUS)
            );

            double knock = EnchantmentHelper.getKnockbackBonus(player);
            if (attributes.hasAttribute(Attributes.ATTACK_KNOCKBACK)) {
                knock += attributes.getValue(Attributes.ATTACK_KNOCKBACK);
            }
            InfoGroupItems damage = InfoGroupItems.group("damage").attrMap(attributes)
                    .addAttr(Attributes.ATTACK_DAMAGE)
                    .addAttr(Attributes.ATTACK_SPEED)
                    .addClientSideFlag(Const.ATTACK_RANGE)
                    .add(Attributes.ATTACK_KNOCKBACK.getDescriptionId(), knock)
                    .addAttr(ExtractAttributes.ARMOR_PENETRATION)
                    .addAttr(ExtractAttributes.DAMAGE_PERCENTAGE_BONUS)
                    .add(Const.CRITICAL_HITS, 1.5F)
                    .addAttr(ExtractAttributes.CRIT_RATE)
                    .addAttr(ExtractAttributes.CRIT_DAMAGE);
            // Damage bound
            getDamageBound(player, damage::add);
            sorted.add(damage);

            int armorValue = player.getArmorValue();
            float armorT = (float) attributes.getValue(Attributes.ARMOR_TOUGHNESS);
            InfoGroupItems armor = InfoGroupItems.group("armor").attrMap(attributes)
                    .add(Attributes.ARMOR.getDescriptionId(), armorValue)
                    .add(Attributes.ARMOR_TOUGHNESS.getDescriptionId(), armorT)
                    .addAttr(Attributes.KNOCKBACK_RESISTANCE)
                    .add(Const.DAMAGE_REDUCTION_BY_ARMOR, 1 - LivingEntityAccessor.get(player).x_PlayerInfo$getDamageAfterArmorAbsorb(damageTest, 1));
            addMagicArmor(player, (name, value) -> armor.add("attribute.extend.armor_bonus." + name, value));
            sorted.add(armor);

            FoodData foodData = player.getFoodData();
            sorted.add(InfoGroupItems.group("food")
                    .add(Const.FOOD_LEVEL, foodData.getFoodLevel())
                    .add(Const.EXHAUSTION_LEVEL, foodData.getExhaustionLevel())
                    .add(Const.SATURATION_LEVEL, foodData.getSaturationLevel())
            );

            sorted.add(InfoGroupItems.group("other").attrMap(attributes)
                    .add(Attributes.MOVEMENT_SPEED.getDescriptionId(), abilities.getWalkingSpeed())
                    .add(Attributes.FLYING_SPEED.getDescriptionId(), abilities.getFlyingSpeed())
                    .addAttr(Attributes.LUCK)
                    .addClientSideFlag(Const.PICK_RANGE)
            );
            return sorted;
        });
    }

    private static void addMagicArmor(Player player, BiConsumer<String, Object> consumer) {
        DamageSources sources = player.level().damageSources();
        LivingEntityAccessor accessor = LivingEntityAccessor.get(player);
        DamageSource source = sources.magic();
        consumer.accept(source.getMsgId(), 1 - accessor.x_PlayerInfo$getDamageAfterMagicAbsorb(source, 1));
        source = sources.fall();
        consumer.accept(source.getMsgId(), 1 - accessor.x_PlayerInfo$getDamageAfterMagicAbsorb(source, 1));
        source = sources.inFire();
        consumer.accept(source.getMsgId(), 1 - accessor.x_PlayerInfo$getDamageAfterMagicAbsorb(source, 1));
        source = sources.freeze();
        consumer.accept(source.getMsgId(), 1 - accessor.x_PlayerInfo$getDamageAfterMagicAbsorb(source, 1));
        source = sources.lightningBolt();
        consumer.accept(source.getMsgId(), 1 - accessor.x_PlayerInfo$getDamageAfterMagicAbsorb(source, 1));
        source = sources.explosion(null, null);
        consumer.accept(source.getMsgId(), 1 - accessor.x_PlayerInfo$getDamageAfterMagicAbsorb(source, 1));
        source = sources.wither();
        consumer.accept(source.getMsgId(), 1 - accessor.x_PlayerInfo$getDamageAfterMagicAbsorb(source, 1));
        source = sources.drown();
        consumer.accept(source.getMsgId(), 1 - accessor.x_PlayerInfo$getDamageAfterMagicAbsorb(source, 1));
    }

    private static void getDamageBound(Player player, BiConsumer<String, Object> consumer) {
        consumer.accept("attribute.extend.damage_bonus.undefined", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEFINED));
        consumer.accept("attribute.extend.damage_bonus.undead", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEAD));
        consumer.accept("attribute.extend.damage_bonus.arthropod", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.ARTHROPOD));
        consumer.accept("attribute.extend.damage_bonus.illager", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.ILLAGER));
        consumer.accept("attribute.extend.damage_bonus.water", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.WATER));
    }
}
