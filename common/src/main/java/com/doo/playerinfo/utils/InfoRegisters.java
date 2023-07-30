package com.doo.playerinfo.utils;

import com.doo.playerinfo.consts.Const;
import com.doo.playerinfo.core.InfoGroupItems;
import com.doo.playerinfo.core.InfoItemCollector;
import com.doo.playerinfo.interfaces.LivingEntityAccessor;
import com.doo.playerinfo.interfaces.OtherPlayerInfoFieldInjector;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;

import static com.doo.playerinfo.consts.Const.MINECRAFT_NAME;

public abstract class InfoRegisters {

    private static DamageSource damageTest;

    private static DamageSource arrowTest;

    private static final BlockState STONE = Blocks.STONE.defaultBlockState();

    private static final Map<String, Map<String, Map<String, List<ValueAttach>>>> MOD_GROUPS_ATTACH_MAP = Maps.newHashMap();

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
            Map<String, Map<String, List<ValueAttach>>> map = MOD_GROUPS_ATTACH_MAP.getOrDefault(MINECRAFT_NAME, Collections.emptyMap());

            String group = "base";
            sorted.add(InfoGroupItems.group(group).attrMap(attributes).canAttach(player, map.getOrDefault(group, Collections.emptyMap()))
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

            group = "movement";
            double extraJump = player.getAttributes().hasAttribute(ExtractAttributes.JUMP_COUNT) ? player.getAttributeValue(ExtractAttributes.JUMP_COUNT) : 0;
            sorted.add(InfoGroupItems.group(group).attrMap(attributes).canAttach(player, map.getOrDefault(group, Collections.emptyMap()))
                    .add(Attributes.MOVEMENT_SPEED.getDescriptionId(), player.getSpeed(), false)
                    .add(Attributes.FLYING_SPEED.getDescriptionId(), OtherPlayerInfoFieldInjector.get(player).playerInfo$getFlySpeed(), false)
                    .add(Const.JUMP_POWER, LivingEntityAccessor.get(player).x_PlayerInfo$getJumpPower(), false)
                    .add(Const.JUMP_COUNT, 1 + extraJump, false)
            );

            group = "xp";
            sorted.add(InfoGroupItems.group(group).attrMap(attributes).canAttach(player, map.getOrDefault(group, Collections.emptyMap()))
                    .add(Const.EXPERIENCE_LEVEL, player.experienceLevel, false)
                    .add(Const.TOTAL_EXPERIENCE, player.totalExperience, false)
                    .add(Const.EXPERIENCE_PROGRESS, player.experienceProgress, true)
                    .addAttr(ExtractAttributes.XP_BONUS, true)
            );

            group = "damage";
            double knock = EnchantmentHelper.getKnockbackBonus(player);
            if (attributes.hasAttribute(Attributes.ATTACK_KNOCKBACK)) {
                knock += attributes.getValue(Attributes.ATTACK_KNOCKBACK);
            }
            InfoGroupItems damage = InfoGroupItems.group(group).attrMap(attributes).canAttach(player, map.getOrDefault(group, Collections.emptyMap()))
                    .addAttr(Attributes.ATTACK_DAMAGE, false)
                    .addAttr(Attributes.ATTACK_SPEED, false)
                    .add(Const.CRITICAL_HITS, 1.5F, true)
                    .addClientSideFlag(Const.ATTACK_RANGE)
                    .addClientSideFlag(Const.ATTACK_SWEEP_RANGE)
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
            getDamageBound(player, (k, v) -> damage.add("attribute.extend.damage_bonus.%s".formatted(k), v, false));
            sorted.add(damage);

            group = "armor";
            int armorValue = player.getArmorValue();
            float armorT = (float) attributes.getValue(Attributes.ARMOR_TOUGHNESS);
            InfoGroupItems armor = InfoGroupItems.group(group).attrMap(attributes).canAttach(player, map.getOrDefault(group, Collections.emptyMap()))
                    .add(Attributes.ARMOR.getDescriptionId(), armorValue, false)
                    .add(Attributes.ARMOR_TOUGHNESS.getDescriptionId(), armorT, false)
                    .addAttr(Attributes.KNOCKBACK_RESISTANCE, true)
                    .add(Const.DAMAGE_REDUCTION_BY_ARMOR, 1 - CombatRules.getDamageAfterAbsorb(1, armorValue, armorT), true);
            addMagicArmor(player, (name, value) -> armor.add("attribute.extend.armor_bonus.%s".formatted(name), value, true));
            sorted.add(armor);

            group = "food";
            FoodData foodData = player.getFoodData();
            sorted.add(InfoGroupItems.group(group).attrMap(attributes).canAttach(player, map.getOrDefault(group, Collections.emptyMap()))
                    .add(Const.FOOD_LEVEL, foodData.getFoodLevel(), false)
                    .add(Const.EXHAUSTION_LEVEL, foodData.getExhaustionLevel(), false)
                    .add(Const.SATURATION_LEVEL, foodData.getSaturationLevel(), false)
                    .addAttr(ExtractAttributes.FOOD_BONUS, true)
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
        consumer.accept("undefined", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEFINED));
        consumer.accept("undead", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.UNDEAD));
        consumer.accept("arthropod", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.ARTHROPOD));
        consumer.accept("illager", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.ILLAGER));
        consumer.accept("water", EnchantmentHelper.getDamageBonus(player.getMainHandItem(), MobType.WATER));
    }

    public static void infoForgeAttach(String group, String key, ToDoubleFunction<Player> valueGetter) {
        regisAttach("Minecraft", group, key, valueGetter::applyAsDouble);
    }

    public static void regisAttach(String modName, String group, String key, ValueAttach attach) {
        MOD_GROUPS_ATTACH_MAP.compute(modName, (k, v) -> {
            if (v == null) {
                v = Maps.newHashMap();
            }

            v.compute(group, (gk, gv) -> {
                if (gv == null) {
                    gv = Maps.newHashMap();
                }

                gv.compute(key, (kk, kv) -> {
                    if (kv == null) {
                        kv = Lists.newArrayList();
                    }

                    kv.add(attach);
                    return kv;
                });

                return gv;
            });
            return v;
        });
    }

    public interface ValueAttach {

        /**
         * Value attach by info collect
         *
         * @param player player
         */
        double get(ServerPlayer player);
    }
}
