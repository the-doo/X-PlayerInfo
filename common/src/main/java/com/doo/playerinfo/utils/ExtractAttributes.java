package com.doo.playerinfo.utils;

import com.doo.playerinfo.XPlayerInfo;
import com.doo.playerinfo.consts.Const;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class ExtractAttributes {

    private ExtractAttributes() {
    }

    public static final Attribute CRIT_DAMAGE = new RangedAttribute("attribute.name.extend.attack.crit_damage", 0, -1024, 1024);
    public static final Attribute CRIT_RATE = new RangedAttribute("attribute.name.extend.attack.crit_rate", 0, 0, 1);
    public static final Attribute XP_BONUS = new RangedAttribute("attribute.name.extend.xp_bonus", 0, 0, 1024);
    public static final Attribute ARMOR_PENETRATION = new RangedAttribute("attribute.name.extend.armor_penetration", 0, 0, 1);
    public static final Attribute HEALING_BONUS = new RangedAttribute("attribute.name.extend.healing_bonus", 0, 0, 1024);
    public static final Attribute HEALING_PER_BONUS = new RangedAttribute("attribute.name.extend.healing_per_bonus", 0, 0, 1024);
    public static final Attribute ABSORPTION_BONUS = new RangedAttribute("attribute.name.extend.absorption_bonus", 0, 0, 1024);
    public static final Attribute DAMAGE_PERCENTAGE_BONUS = new RangedAttribute("attribute.name.extend.attack.damage_percentage_bonus", 0, 0, 1);
    public static final Attribute ATTACK_RANGE = new RangedAttribute(Const.ATTACK_RANGE, 0, 0, 1024).setSyncable(true);
    public static final Attribute BOW_USING_SPEED = new RangedAttribute("attribute.name.extend.attack.bow_using_speed", 0, 0, 1024);
    public static final Attribute BOW_DAMAGE_BONUS = new RangedAttribute("attribute.name.extend.attack.bow_damage_bonus", 0, 0, 1024);
    public static final Attribute ATTACK_HEALING = new RangedAttribute("attribute.name.extend.attack.attack_healing", 0, 0, 1024);
    public static final Attribute DAMAGE_PERCENTAGE_HEALING = new RangedAttribute("attribute.name.extend.attack.damage_percentage_healing", 0, 0, 1024);
    public static final Attribute JUMP_STRENGTH_BONUS = new RangedAttribute("attribute.name.extend.jump.strength_bonus", 0, 0, 1024).setSyncable(true);
    public static final Attribute JUMP_COUNT = new RangedAttribute("attribute.name.extend.jump.extra_count", 0, 0, 1024).setSyncable(true);
    public static final Attribute FOOD_BONUS = new RangedAttribute("attribute.name.extend.food_bonus", 0, 0, 1024);
    public static final Attribute TOUCH_RANGE_BONUS = new RangedAttribute("attribute.name.extend.touch_range_bonus", 0, 0, 1024);

    public static final Set<Attribute> TOTAL_ATTRS = new HashSet<>();

    static {
        TOTAL_ATTRS.add(CRIT_RATE);
        TOTAL_ATTRS.add(CRIT_DAMAGE);
        TOTAL_ATTRS.add(XP_BONUS);
        TOTAL_ATTRS.add(ARMOR_PENETRATION);
        TOTAL_ATTRS.add(HEALING_BONUS);
        TOTAL_ATTRS.add(HEALING_PER_BONUS);
        TOTAL_ATTRS.add(ABSORPTION_BONUS);
        TOTAL_ATTRS.add(DAMAGE_PERCENTAGE_BONUS);
        TOTAL_ATTRS.add(BOW_USING_SPEED);
        TOTAL_ATTRS.add(BOW_DAMAGE_BONUS);
        TOTAL_ATTRS.add(ATTACK_HEALING);
        TOTAL_ATTRS.add(DAMAGE_PERCENTAGE_HEALING);
        TOTAL_ATTRS.add(JUMP_STRENGTH_BONUS);
        TOTAL_ATTRS.add(JUMP_COUNT);
        TOTAL_ATTRS.add(FOOD_BONUS);
        TOTAL_ATTRS.add(TOUCH_RANGE_BONUS);
    }

    public static void register(Consumer<Attribute> attributeConsumer) {
        TOTAL_ATTRS.forEach(attributeConsumer);
    }

    public static void createAttrToLiving(AttributeSupplier.Builder builder) {
        builder.add(CRIT_DAMAGE)
                .add(CRIT_RATE)
                .add(DAMAGE_PERCENTAGE_BONUS)
                .add(ARMOR_PENETRATION)
                .add(HEALING_BONUS)
                .add(HEALING_PER_BONUS)
                .add(BOW_USING_SPEED)
                .add(BOW_DAMAGE_BONUS)
                .add(ATTACK_HEALING)
                .add(DAMAGE_PERCENTAGE_HEALING)
                .add(JUMP_STRENGTH_BONUS)
                .add(JUMP_COUNT)
        ;
    }

    public static void createAttrToPlayer(AttributeSupplier.Builder builder) {
        builder.add(XP_BONUS)
                .add(ABSORPTION_BONUS)
                .add(FOOD_BONUS)
                .add(TOUCH_RANGE_BONUS)
        ;

        if (!XPlayerInfo.isForge()) {
            fabricCreateAttrToPlayer(builder);
        }
    }

    public static void fabricRegister(Consumer<Attribute> attributeConsumer) {
        attributeConsumer.accept(ATTACK_RANGE);

        register(attributeConsumer);
    }

    public static void forgeRegister(Consumer<Attribute> attributeConsumer) {
        register(attributeConsumer);
    }

    public static void fabricCreateAttrToPlayer(AttributeSupplier.Builder builder) {
        builder.add(ATTACK_RANGE);
    }

    public static double get(AttributeMap attributes, Attribute attribute) {
        return attributes.hasAttribute(attribute) ? attributes.getValue(attribute) : 0;
    }

    public static void playerTouchItems(Player player, AABB box) {
        double range = get(player.getAttributes(), ExtractAttributes.TOUCH_RANGE_BONUS);
        if (range == 0) {
            return;
        }

        range += 1;
        AABB newBox = box.inflate(range, range / 2, range);
        for (Entity item : player.level().getEntities(player, newBox)) {
            if (item instanceof LivingEntity) {
                continue;
            }
            item.playerTouch(player);
        }
    }
}
