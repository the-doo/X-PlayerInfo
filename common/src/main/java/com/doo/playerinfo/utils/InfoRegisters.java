package com.doo.playerinfo.utils;

import com.doo.playerinfo.attributes.ExtractAttributes;
import com.doo.playerinfo.consts.Const;
import com.doo.playerinfo.core.InfoGroupItems;
import com.doo.playerinfo.core.InfoItemCollector;
import com.google.common.collect.Lists;
import net.minecraft.world.damagesource.CombatRules;
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

    private InfoRegisters() {
    }

    public static void initMinecraft() {
        InfoItemCollector.register(MINECRAFT_NAME, player -> {
            List<InfoGroupItems> sorted = Lists.newArrayList();
            AttributeMap attributes = player.getAttributes();
            Abilities abilities = player.getAbilities();

            sorted.add(InfoGroupItems.group("base")
                    .add(Const.HEALTH, player.getHealth())
                    .add(Attributes.MAX_HEALTH.getDescriptionId(), attributes.getValue(Attributes.MAX_HEALTH))
                    .add(Const.ABSORPTION_AMOUNT, player.getAbsorptionAmount())
            );

            sorted.add(InfoGroupItems.group("xp")
                    .add(Const.EXPERIENCE_LEVEL, player.experienceLevel)
                    .add(Const.TOTAL_EXPERIENCE, player.totalExperience)
                    .add(Const.EXPERIENCE_PROGRESS, player.experienceProgress)
                    .add(ExtractAttributes.EX_XP.getDescriptionId(), attributes.getValue(ExtractAttributes.EX_XP))
            );

            double knock = EnchantmentHelper.getKnockbackBonus(player);
            if (attributes.hasAttribute(Attributes.ATTACK_KNOCKBACK)) {
                knock += attributes.getValue(Attributes.ATTACK_KNOCKBACK);
            }
            InfoGroupItems damage = InfoGroupItems.group("damage")
                    .add(Attributes.ATTACK_DAMAGE.getDescriptionId(), attributes.getValue(Attributes.ATTACK_DAMAGE))
                    .add(Attributes.ATTACK_SPEED.getDescriptionId(), attributes.getValue(Attributes.ATTACK_SPEED))
                    .add(Attributes.ATTACK_KNOCKBACK.getDescriptionId(), knock)
                    .add(ExtractAttributes.ARMOR_PENETRATION.getDescriptionId(), attributes.getValue(ExtractAttributes.ARMOR_PENETRATION))
                    .add(Const.CRITICAL_HITS, 1.5F)
                    .add(ExtractAttributes.CRIT_RATE.getDescriptionId(), attributes.getValue(ExtractAttributes.CRIT_RATE))
                    .add(ExtractAttributes.CRIT_DAMAGE.getDescriptionId(), attributes.getValue(ExtractAttributes.CRIT_DAMAGE));
            // Damage bound
            initDamageBound(player, damage::add);
            sorted.add(damage);

            int armor = player.getArmorValue();
            float armorT = (float) attributes.getValue(Attributes.ARMOR_TOUGHNESS);
            double damageR = 1 - CombatRules.getDamageAfterAbsorb(1F, armor, armorT);
            sorted.add(InfoGroupItems.group("armor")
                    .add(Attributes.ARMOR.getDescriptionId(), armor)
                    .add(Attributes.ARMOR_TOUGHNESS.getDescriptionId(), armorT)
                    .add(Const.DAMAGE_REDUCTION_BY_ARMOR, damageR)
                    .add(Attributes.KNOCKBACK_RESISTANCE.getDescriptionId(), attributes.getValue(Attributes.KNOCKBACK_RESISTANCE))
            );

            FoodData foodData = player.getFoodData();
            sorted.add(InfoGroupItems.group("food")
                    .add(Const.FOOD_LEVEL, foodData.getFoodLevel())
                    .add(Const.EXHAUSTION_LEVEL, foodData.getExhaustionLevel())
                    .add(Const.SATURATION_LEVEL, foodData.getSaturationLevel())
            );

            sorted.add(InfoGroupItems.group("other")
                    .add(Attributes.MOVEMENT_SPEED.getDescriptionId(), abilities.getWalkingSpeed())
                    .add(Attributes.FLYING_SPEED.getDescriptionId(), abilities.getFlyingSpeed())
                    .add(Attributes.LUCK.getDescriptionId(), attributes.getValue(Attributes.LUCK))
                    .addClientSideFlag(Const.PICK_RANGE)
            );
            return sorted;
        });
    }

    private static void initDamageBound(Player player, BiConsumer<String, Object> consumer) {
        consumer.accept("attribute.extend.damage_bonus.undefined", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEFINED));
        consumer.accept("attribute.extend.damage_bonus.undead", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEAD));
        consumer.accept("attribute.extend.damage_bonus.arthropod", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.ARTHROPOD));
        consumer.accept("attribute.extend.damage_bonus.illager", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.ILLAGER));
        consumer.accept("attribute.extend.damage_bonus.water", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.WATER));
    }
}
