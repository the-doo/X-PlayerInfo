package com.doo.playerinfo.mixin;

import com.doo.playerinfo.attributes.ExtractAttributes;
import com.doo.playerinfo.utils.DamageSourceUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract double getAttributeValue(Attribute attribute);

    @Shadow
    public abstract float getMaxHealth();

    @Unique
    private DamageSource x_PlayerInfo$currentDamageSource;

    @Inject(method = "createLivingAttributes", at = @At(value = "RETURN"))
    private static void injectedCreateAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        ExtractAttributes.createAttrToLiving(cir.getReturnValue());
    }

    @ModifyVariable(method = "actuallyHurt", at = @At(value = "STORE", ordinal = 0), argsOnly = true)
    private float x_PlayerInfo$damageAmount(float amount, DamageSource source) {
        x_PlayerInfo$currentDamageSource = source;
        return DamageSourceUtil.additionDamage(source, amount, getMaxHealth());
    }

    @ModifyArg(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(FFF)F"), index = 1)
    private float injectedIgnoredArmorAttributes(float f) {
        return DamageSourceUtil.reductionFromArmor(x_PlayerInfo$currentDamageSource, f);
    }

    @ModifyVariable(method = "heal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"), argsOnly = true)
    private float injectedHealAttributes(float f) {
        return f * (1 + (float) getAttributeValue(ExtractAttributes.HEALING_BONUS));
    }
}
