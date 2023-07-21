package com.doo.playerinfo.interfaces;

import com.doo.playerinfo.XPlayerInfo;
import net.minecraft.world.damagesource.DamageSource;

public interface LivingEntityAccessor {

    static LivingEntityAccessor get(Object player) {
        return XPlayerInfo.get(player);
    }

    float x_PlayerInfo$getDamageAfterMagicAbsorb(DamageSource arg, float g);

    void x_PlayerInfo$addDamageHealing(float healing);

    void x_PlayerInfo$healingPlayer();

    void x_PlayerInfo$resetHealing();
}
