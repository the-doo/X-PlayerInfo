package com.doo.playerinfo.attributes;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.function.Consumer;

public class ExtractAttributes {

    private ExtractAttributes() {
    }

    public static final Attribute CRIT_DAMAGE = new RangedAttribute("attribute.name.extend.attack.crit_damage", 0, 0, 1024);
    public static final Attribute CRIT_RATE = new RangedAttribute("attribute.name.extend.attack.crit_rate", 0, 0, 100);
    public static final Attribute EX_XP = new RangedAttribute("attribute.name.extend.xp_bonus", 0, 0, 1024);
    public static final Attribute ARMOR_PENETRATION = new RangedAttribute("attribute.name.extend.armor_penetration", 0, 0, 100);

    public static void register(Consumer<Attribute> attributeConsumer) {
        attributeConsumer.accept(CRIT_RATE);
        attributeConsumer.accept(CRIT_DAMAGE);
        attributeConsumer.accept(EX_XP);
        attributeConsumer.accept(ARMOR_PENETRATION);
    }

    public static void createAttrToLiving(AttributeSupplier.Builder builder) {
        builder.add(ExtractAttributes.CRIT_DAMAGE)
                .add(ExtractAttributes.CRIT_RATE)
                .add(ExtractAttributes.ARMOR_PENETRATION);
    }

    public static void createAttrToPlayer(AttributeSupplier.Builder builder) {
        builder.add(ExtractAttributes.EX_XP);
    }
}
