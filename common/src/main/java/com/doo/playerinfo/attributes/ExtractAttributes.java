package com.doo.playerinfo.attributes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import java.util.function.Consumer;

public class ExtractAttributes {

    private ExtractAttributes() {
    }

    public static final Attribute CRIT_DAMAGE = new RangedAttribute("attribute.name.extend.attack.crit_damage", 0, 0, 1024);
    public static final Attribute CRIT_RATE = new RangedAttribute("attribute.name.extend.attack.crit_rate", 0, 0, 100);
    public static final Attribute EX_XP = new RangedAttribute("attribute.name.extend.xp_bonus", 0, 0, 1024);

    public static void initFabric() {
        Registry.register(BuiltInRegistries.ATTRIBUTE, CRIT_RATE.getDescriptionId(), CRIT_RATE);
        Registry.register(BuiltInRegistries.ATTRIBUTE, CRIT_DAMAGE.getDescriptionId(), CRIT_DAMAGE);
        Registry.register(BuiltInRegistries.ATTRIBUTE, EX_XP.getDescriptionId(), EX_XP);
    }

    public static void registerForge(Consumer<Attribute> attributeConsumer) {
        attributeConsumer.accept(CRIT_RATE);
        attributeConsumer.accept(CRIT_DAMAGE);
        attributeConsumer.accept(EX_XP);
    }
}
