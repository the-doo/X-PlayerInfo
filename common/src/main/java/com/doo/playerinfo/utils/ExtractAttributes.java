package com.doo.playerinfo.utils;

import com.doo.playerinfo.XPlayerInfo;
import com.doo.playerinfo.consts.Const;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.function.Consumer;

public class ExtractAttributes {

    private ExtractAttributes() {
    }

    public static final Attribute CRIT_DAMAGE = new RangedAttribute("attribute.name.extend.attack.crit_damage", 0, 0, 1024);
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

    public static void register(Consumer<Attribute> attributeConsumer) {
        attributeConsumer.accept(CRIT_RATE);
        attributeConsumer.accept(CRIT_DAMAGE);
        attributeConsumer.accept(XP_BONUS);
        attributeConsumer.accept(ARMOR_PENETRATION);
        attributeConsumer.accept(HEALING_BONUS);
        attributeConsumer.accept(HEALING_PER_BONUS);
        attributeConsumer.accept(ABSORPTION_BONUS);
        attributeConsumer.accept(DAMAGE_PERCENTAGE_BONUS);
        attributeConsumer.accept(BOW_USING_SPEED);
        attributeConsumer.accept(BOW_DAMAGE_BONUS);
        attributeConsumer.accept(ATTACK_HEALING);
        attributeConsumer.accept(DAMAGE_PERCENTAGE_HEALING);
        attributeConsumer.accept(JUMP_STRENGTH_BONUS);
        attributeConsumer.accept(JUMP_COUNT);
        attributeConsumer.accept(FOOD_BONUS);
        attributeConsumer.accept(TOUCH_RANGE_BONUS);
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

        if (XPlayerInfo.isFabric()) {
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
        if (range != 0) {
            return;
        }

        range += 1;
        AABB newBox = box.inflate(range, 0, range);
        for (Entity item : player.level().getEntities(player, newBox)) {
            if (item instanceof Monster) {
                continue;
            }
            item.playerTouch(player);
        }
    }
}
