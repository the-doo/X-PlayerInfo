package com.doo.playerinfo.mixin;

import com.doo.playerinfo.attributes.ExtractAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "createLivingAttributes", at = @At(value = "RETURN"))
    private static void injectedCreateAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue().add(ExtractAttributes.CRIT_DAMAGE).add(ExtractAttributes.CRIT_RATE);
    }
}
