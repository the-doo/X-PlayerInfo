package com.doo.playerinfo.attributes;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class ExtractAttributes {

    public static final Attribute CRIT_DAMAGE = new RangedAttribute("attribute.name.extend.attack.crit_damage", 0, 0, 1024);
    ;
    public static final Attribute CRIT_RATE = new RangedAttribute("attribute.name.extend.attack.crit_rate", 0, 0, 100);

    public static void initFabric() {
        Registry.register(BuiltInRegistries.ATTRIBUTE, CRIT_RATE.getDescriptionId(), CRIT_RATE);
        Registry.register(BuiltInRegistries.ATTRIBUTE, CRIT_DAMAGE.getDescriptionId(), CRIT_DAMAGE);
    }
}
