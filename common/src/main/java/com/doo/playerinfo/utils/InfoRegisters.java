package com.doo.playerinfo.utils;

import com.doo.playerinfo.attributes.ExtractAttributes;
import com.doo.playerinfo.consts.Const;
import com.doo.playerinfo.core.InfoGroupItems;
import com.doo.playerinfo.core.InfoItemCollector;
import com.doo.playerinfo.interfaces.LivingEntityAccessor;
import com.doo.playerinfo.interfaces.OtherPlayerInfoFieldInjector;
import com.google.common.collect.Lists;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.ObjDoubleConsumer;

import static com.doo.playerinfo.consts.Const.MINECRAFT_NAME;

public abstract class InfoRegisters {

    private static DamageSource damageTest;

    private static DamageSource arrowTest;

    private static final BlockState STONE = Blocks.STONE.defaultBlockState();

    private InfoRegisters() {
    }

    public static void initMinecraft() {
        InfoItemCollector.register(MINECRAFT_NAME, player -> {
            if (damageTest == null) {
                damageTest = player.level().damageSources().mobAttack(null);
                Arrow arrow = new Arrow(player.level(), player);
                arrow.setBaseDamage(1);
                arrowTest = player.level().damageSources().arrow(arrow, null);
            }

            List<InfoGroupItems> sorted = Lists.newArrayList();
            AttributeMap attributes = player.getAttributes();

            sorted.add(InfoGroupItems.group("base").attrMap(attributes)
                    .add(Const.HEALTH, player.getHealth(), false)
                    .addAttr(Attributes.MAX_HEALTH, false)
                    .addAttr(ExtractAttributes.HEALING_BONUS, true)
                    .add(Const.ABSORPTION_AMOUNT, player.getAbsorptionAmount(), false)
                    .addAttr(ExtractAttributes.ABSORPTION_BONUS, true)
                    .addAttr(Attributes.LUCK, false)
                    .add(Const.DIGGER_EFFICIENCY, player.getDestroySpeed(STONE) / 45, false)
                    .addClientSideFlag(Const.DIGGER_LEVEL)
                    .addClientSideFlag(Const.DIGGER_SPEED)
                    .addClientSideFlag(Const.PICK_RANGE)
            );

            double extraJump = player.getAttributes().hasAttribute(ExtractAttributes.JUMP_COUNT) ? player.getAttributeValue(ExtractAttributes.JUMP_COUNT) : 0;
            sorted.add(InfoGroupItems.group("movement").attrMap(attributes)
                    .add(Attributes.MOVEMENT_SPEED.getDescriptionId(), player.getSpeed(), false)
                    .add(Attributes.FLYING_SPEED.getDescriptionId(), OtherPlayerInfoFieldInjector.get(player).playerInfo$getFlySpeed(), false)
                    .add(Const.JUMP_POWER, LivingEntityAccessor.get(player).x_PlayerInfo$getJumpPower(), false)
                    .add(Const.JUMP_COUNT, 1 + extraJump, false)
            );

            sorted.add(InfoGroupItems.group("xp").attrMap(attributes)
                    .add(Const.EXPERIENCE_LEVEL, player.experienceLevel, false)
                    .add(Const.TOTAL_EXPERIENCE, player.totalExperience, false)
                    .add(Const.EXPERIENCE_PROGRESS, player.experienceProgress, true)
                    .addAttr(ExtractAttributes.XP_BONUS, true)
            );

            double knock = EnchantmentHelper.getKnockbackBonus(player);
            if (attributes.hasAttribute(Attributes.ATTACK_KNOCKBACK)) {
                knock += attributes.getValue(Attributes.ATTACK_KNOCKBACK);
            }
            InfoGroupItems damage = InfoGroupItems.group("damage").attrMap(attributes)
                    .addAttr(Attributes.ATTACK_DAMAGE, false)
                    .addAttr(Attributes.ATTACK_SPEED, false)
                    .add(Const.CRITICAL_HITS, 1.5F, false)
                    .addClientSideFlag(Const.ATTACK_RANGE)
                    .addAttr(ExtractAttributes.ATTACK_HEALING, false)
                    .addAttr(ExtractAttributes.DAMAGE_PERCENTAGE_HEALING, true)
                    .addAttr(ExtractAttributes.CRIT_RATE, true)
                    .addAttr(ExtractAttributes.CRIT_DAMAGE, true)
                    .add(Attributes.ATTACK_KNOCKBACK.getDescriptionId(), knock, false)
                    .addAttr(ExtractAttributes.BOW_USING_SPEED, true)
                    .addAttr(ExtractAttributes.BOW_DAMAGE_BONUS, true)
                    .addAttr(ExtractAttributes.ARMOR_PENETRATION, true)
                    .addAttr(ExtractAttributes.DAMAGE_PERCENTAGE_BONUS, true);
            // Damage bound
            getDamageBound(player, (k, v) -> damage.add(k, v, false));
            sorted.add(damage);

            int armorValue = player.getArmorValue();
            float armorT = (float) attributes.getValue(Attributes.ARMOR_TOUGHNESS);
            InfoGroupItems armor = InfoGroupItems.group("armor").attrMap(attributes)
                    .add(Attributes.ARMOR.getDescriptionId(), armorValue, false)
                    .add(Attributes.ARMOR_TOUGHNESS.getDescriptionId(), armorT, false)
                    .addAttr(Attributes.KNOCKBACK_RESISTANCE, true)
                    .add(Const.DAMAGE_REDUCTION_BY_ARMOR, 1 - CombatRules.getDamageAfterAbsorb(1, armorValue, armorT), true);
            addMagicArmor(player, (name, value) -> armor.add("attribute.extend.armor_bonus." + name, value, true));
            sorted.add(armor);

            FoodData foodData = player.getFoodData();
            sorted.add(InfoGroupItems.group("food")
                    .add(Const.FOOD_LEVEL, foodData.getFoodLevel(), false)
                    .add(Const.EXHAUSTION_LEVEL, foodData.getExhaustionLevel(), false)
                    .add(Const.SATURATION_LEVEL, foodData.getSaturationLevel(), false)
            );

            return sorted;
        });
    }

    private static void addMagicArmor(Player player, ObjDoubleConsumer<String> consumer) {
        DamageSources sources = player.level().damageSources();
        LivingEntityAccessor accessor = LivingEntityAccessor.get(player);
        DamageSource source = arrowTest;
        consumer.accept(source.getMsgId(), 1 - accessor.x_PlayerInfo$getDamageAfterMagicAbsorb(source, 1));
        source = sources.magic();
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

    private static void getDamageBound(Player player, ObjDoubleConsumer<String> consumer) {
        consumer.accept("attribute.extend.damage_bonus.undefined", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEFINED));
        consumer.accept("attribute.extend.damage_bonus.undead", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEAD));
        consumer.accept("attribute.extend.damage_bonus.arthropod", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.ARTHROPOD));
        consumer.accept("attribute.extend.damage_bonus.illager", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.ILLAGER));
        consumer.accept("attribute.extend.damage_bonus.water", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.WATER));
    }
}
