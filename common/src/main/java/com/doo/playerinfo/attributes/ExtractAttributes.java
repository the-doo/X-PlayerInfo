package com.doo.playerinfo.attributes;

import com.doo.playerinfo.XPlayerInfo;
import com.doo.playerinfo.consts.Const;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.function.Consumer;

public class ExtractAttributes {

    private ExtractAttributes() {
    }

    public static final Attribute CRIT_DAMAGE = new RangedAttribute("attribute.name.extend.attack.crit_damage", 0.5, 0, 1024);
    public static final Attribute CRIT_RATE = new RangedAttribute("attribute.name.extend.attack.crit_rate", 0, 0, 1);
    public static final Attribute XP_BONUS = new RangedAttribute("attribute.name.extend.xp_bonus", 0, 0, 1024);
    public static final Attribute ARMOR_PENETRATION = new RangedAttribute("attribute.name.extend.armor_penetration", 0, 0, 1);
    public static final Attribute HEALING_BONUS = new RangedAttribute("attribute.name.extend.healing_bonus", 0, 0, 1024);
    public static final Attribute ABSORPTION_BONUS = new RangedAttribute("attribute.name.extend.absorption_bonus", 0, 0, 1024);
    public static final Attribute DAMAGE_PERCENTAGE_BONUS = new RangedAttribute("attribute.name.extend.attack.damage_percentage_bonus", 0, 0, 1);
    public static final Attribute ATTACK_RANGE = new RangedAttribute(Const.ATTACK_RANGE, 0, 0, 1024);

    public static void register(Consumer<Attribute> attributeConsumer) {
        attributeConsumer.accept(CRIT_RATE);
        attributeConsumer.accept(CRIT_DAMAGE);
        attributeConsumer.accept(XP_BONUS);
        attributeConsumer.accept(ARMOR_PENETRATION);
        attributeConsumer.accept(HEALING_BONUS);
        attributeConsumer.accept(ABSORPTION_BONUS);
        attributeConsumer.accept(DAMAGE_PERCENTAGE_BONUS);
    }

    public static void createAttrToLiving(AttributeSupplier.Builder builder) {
        builder.add(CRIT_DAMAGE)
                .add(CRIT_RATE)
                .add(DAMAGE_PERCENTAGE_BONUS)
                .add(ARMOR_PENETRATION)
                .add(HEALING_BONUS)
        ;
    }

    public static void createAttrToPlayer(AttributeSupplier.Builder builder) {
        builder.add(XP_BONUS)
                .add(ABSORPTION_BONUS)
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
}
