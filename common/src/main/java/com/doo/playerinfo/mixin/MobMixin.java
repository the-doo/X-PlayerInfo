package com.doo.playerinfo.mixin;

import com.doo.playerinfo.attributes.ExtractAttributes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyVariable(method = "doHurtTarget", at = @At(value = "STORE", ordinal = 1), ordinal = 0)
    private float injectOtherCriticalHits(float value) {
        if (level() instanceof ServerLevel && random.nextInt(0, 10000) / 100d <= getAttributeValue(ExtractAttributes.CRIT_RATE)) {
            value *= (1 + (float) (getAttributeValue(ExtractAttributes.CRIT_DAMAGE)));
        }
        return value;
    }
}
