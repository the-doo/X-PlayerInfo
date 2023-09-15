package com.doo.playerinfo.utils;

import com.doo.playerinfo.consts.Const;
import com.doo.playerinfo.core.InfoGroupItems;
import com.doo.playerinfo.core.InfoItemCollector;
import com.doo.playerinfo.interfaces.LivingEntityAccessor;
import com.doo.playerinfo.interfaces.OtherPlayerInfoFieldInjector;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import static com.doo.playerinfo.consts.Const.MINECRAFT_NAME;

public abstract class InfoRegisters {

    private static final String DAMAGE_BONUS_KEY_FORMAT = "attribute.extend.damage_bonus.%s";
    private static final String ARMOR_BONUS_KEY_FORMAT = "attribute.extend.armor_bonus.%s";

    private static final BlockState STONE = Blocks.STONE.defaultBlockState();

    private static final Map<String, Map<String, Map<String, List<ValueAttach>>>> MOD_GROUPS_ATTACH_MAP = Maps.newHashMap();

    private static DamageSource damageTest;

    private static DamageSource arrowTest;

    private static final Map<String, MobType> MOB_TYPE_MAP = new HashMap<>();

    private static final Stat<ResourceLocation> DEATH_STAT = Stats.CUSTOM.get(Stats.DEATHS);
    private static final Stat<ResourceLocation> PLAYER_KILLS_STAT = Stats.CUSTOM.get(Stats.PLAYER_KILLS);
    private static final Stat<ResourceLocation> MOB_KILLS_STAT = Stats.CUSTOM.get(Stats.MOB_KILLS);

    private InfoRegisters() {
    }

    public static void initMinecraft() {
        MOB_TYPE_MAP.put("undefined", MobType.UNDEFINED);
        MOB_TYPE_MAP.put("undead", MobType.UNDEAD);
        MOB_TYPE_MAP.put("arthropod", MobType.ARTHROPOD);
        MOB_TYPE_MAP.put("illager", MobType.ILLAGER);
        MOB_TYPE_MAP.put("water", MobType.WATER);

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
                    .add(Const.HEALTH, player.getHealth())
                    .addAttr(Attributes.MAX_HEALTH)
                    .addAttr(ExtractAttributes.HEALING_BONUS, true)
                    .addAttr(ExtractAttributes.HEALING_PER_BONUS)
                    .add(Const.ABSORPTION_AMOUNT, player.getAbsorptionAmount())
                    .addAttr(ExtractAttributes.ABSORPTION_BONUS, true)
                    .addAttr(Attributes.LUCK)
                    .addClientSideFlag(Const.PICK_RANGE)
                    .addAttr(ExtractAttributes.TOUCH_RANGE_BONUS)
            );

            group = "movement";
            double extraJump = player.getAttributes().hasAttribute(ExtractAttributes.JUMP_COUNT) ? player.getAttributeValue(ExtractAttributes.JUMP_COUNT) : 0;
            sorted.add(InfoGroupItems.group(group).attrMap(attributes).canAttach(player, map.getOrDefault(group, Collections.emptyMap()))
                    .add(Attributes.MOVEMENT_SPEED.getDescriptionId(), player.getSpeed())
                    .add(Attributes.FLYING_SPEED.getDescriptionId(), OtherPlayerInfoFieldInjector.get(player).playerInfo$getFlySpeed())
                    .add(Const.JUMP_POWER, LivingEntityAccessor.get(player).x_PlayerInfo$getJumpPower())
                    .add(Const.JUMP_COUNT, 1 + extraJump)
                    .addAttr(ExtractAttributes.JUMP_STRENGTH_BONUS)
            );

            group = "damage";
            double knock = EnchantmentHelper.getKnockbackBonus(player) + ExtractAttributes.get(attributes, Attributes.ATTACK_KNOCKBACK);
            InfoGroupItems damage = InfoGroupItems.group(group).attrMap(attributes).canAttach(player, map.getOrDefault(group, Collections.emptyMap()))
                    .addAttr(Attributes.ATTACK_DAMAGE)
                    .addAttr(Attributes.ATTACK_SPEED)
                    .add(Const.CRITICAL_HITS, 1.5F, true)
                    .addClientSideFlag(Const.ATTACK_RANGE)
                    .addClientSideFlag(Const.ATTACK_SWEEP_RANGE)
                    .addAttr(ExtractAttributes.ATTACK_HEALING)
                    .addAttr(ExtractAttributes.DAMAGE_PERCENTAGE_HEALING, true)
                    .addAttr(ExtractAttributes.CRIT_RATE, true)
                    .addAttr(ExtractAttributes.CRIT_DAMAGE, true)
                    .add(Attributes.ATTACK_KNOCKBACK.getDescriptionId(), knock)
                    .addAttr(ExtractAttributes.BOW_USING_SPEED, true)
                    .addAttr(ExtractAttributes.BOW_DAMAGE_BONUS, true)
                    .addAttr(ExtractAttributes.ARMOR_PENETRATION, true)
                    .addAttr(ExtractAttributes.DAMAGE_PERCENTAGE_BONUS, true);
            // Damage bound
            getDamageBound(player, (k, v) -> damage.add(DAMAGE_BONUS_KEY_FORMAT.formatted(k), v));
            sorted.add(damage);

            group = "armor";
            int armorValue = player.getArmorValue();
            float armorT = (float) attributes.getValue(Attributes.ARMOR_TOUGHNESS);
            InfoGroupItems armor = InfoGroupItems.group(group).attrMap(attributes).canAttach(player, map.getOrDefault(group, Collections.emptyMap()))
                    .add(Attributes.ARMOR.getDescriptionId(), armorValue)
                    .add(Attributes.ARMOR_TOUGHNESS.getDescriptionId(), armorT)
                    .addAttr(Attributes.KNOCKBACK_RESISTANCE, true);

            float f = DamageSourceUtil.test(player, damageTest, 1);
            armor.add(Const.DAMAGE_REDUCTION_BY_ARMOR, 1 - f, true);
            addMagicArmor(player, (name, value) -> armor.add(ARMOR_BONUS_KEY_FORMAT.formatted(name), value, true));
            sorted.add(armor);

            group = "digger";
            sorted.add(InfoGroupItems.group(group).attrMap(attributes).canAttach(player, map.getOrDefault(group, Collections.emptyMap()))
                    .add(Const.DIGGER_EFFICIENCY, player.getDestroySpeed(STONE) / 45)
                    .addClientSideFlag(Const.DIGGER_LEVEL)
                    .addClientSideFlag(Const.DIGGER_SPEED)
            );

            group = "xp";
            sorted.add(InfoGroupItems.group(group).attrMap(attributes).canAttach(player, map.getOrDefault(group, Collections.emptyMap()))
                    .add(Const.EXPERIENCE_LEVEL, player.experienceLevel)
                    .add(Const.TOTAL_EXPERIENCE, player.totalExperience)
                    .add(Const.EXPERIENCE_PROGRESS, player.experienceProgress, true)
                    .addAttr(ExtractAttributes.XP_BONUS, true)
            );

            group = "food";
            FoodData foodData = player.getFoodData();
            sorted.add(InfoGroupItems.group(group).attrMap(attributes).canAttach(player, map.getOrDefault(group, Collections.emptyMap()))
                    .add(Const.FOOD_LEVEL, foodData.getFoodLevel())
                    .add(Const.EXHAUSTION_LEVEL, foodData.getExhaustionLevel())
                    .add(Const.SATURATION_LEVEL, foodData.getSaturationLevel())
                    .addAttr(ExtractAttributes.FOOD_BONUS, true)
            );

            group = "stats";
            ServerStatsCounter stats = player.getStats();
            sorted.add(InfoGroupItems.group(group).attrMap(attributes).canAttach(player, map.getOrDefault(group, Collections.emptyMap()))
                    .add(Const.SCORE, player.getScore())
                    .add(Const.DEATH_COUNT, stats.getValue(DEATH_STAT))
                    .add(Const.PLAYER_KILL_COUNT, stats.getValue(PLAYER_KILLS_STAT))
                    .add(Const.MOB_KILL_COUNT, stats.getValue(MOB_KILLS_STAT))
            );

            return sorted;
        });

        regisAttach("Minecraft", "base", ExtractAttributes.HEALING_PER_BONUS.getDescriptionId(), player -> {
            MobEffectInstance effect = player.getEffect(MobEffects.REGENERATION);
            if (effect != null) {
                return 20D / (50 >> effect.getAmplifier());
            }
            return 0;
        });
    }

    private static void addMagicArmor(ServerPlayer player, ObjDoubleConsumer<String> consumer) {
        DamageSources sources = player.level().damageSources();

        Stream.of(
                arrowTest, sources.magic(), sources.fall(), sources.inFire(),
                sources.thorns(null), sources.freeze(), sources.lightningBolt(),
                sources.explosion(null, null), sources.wither(), sources.drown(),
                sources.starve()
        ).forEach(source -> {
            float f = DamageSourceUtil.test(player, source, 1);
            consumer.accept(source.getMsgId(), 1 - f);
        });
    }

    private static void getDamageBound(Player player, ObjDoubleConsumer<String> consumer) {
        MOB_TYPE_MAP.forEach((k, v) -> consumer.accept(k, EnchantmentHelper.getDamageBonus(player.getMainHandItem(), v)));
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
