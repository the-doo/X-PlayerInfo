package com.doo.playerinfo.interfaces;

import com.doo.playerinfo.XPlayerInfo;
import net.minecraft.world.damagesource.DamageSource;

public interface LivingEntityAccessor {

    static LivingEntityAccessor get(Object player) {
        return XPlayerInfo.get(player);
    }

    float getDamageAfterArmorAbsorb(DamageSource arg, float g);

    float getDamageAfterMagicAbsorb(DamageSource arg, float g);
}
