package com.doo.playerinfo.mixin;

import com.doo.playerinfo.attributes.ExtractAttributes;
import com.doo.playerinfo.interfaces.LivingEntityAccessor;
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
public abstract class LivingEntityMixin implements LivingEntityAccessor {

    @Shadow
    public abstract double getAttributeValue(Attribute attribute);

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    protected abstract float getDamageAfterArmorAbsorb(DamageSource damageSource, float f);

    @Shadow
    protected abstract float getDamageAfterMagicAbsorb(DamageSource damageSource, float f);

    @Shadow
    public abstract void heal(float f);

    @Unique
    private DamageSource x_PlayerInfo$currentDamageSource;
    @Unique
    private float x_PlayerInfo$lastDamageHealing;

    @Inject(method = "createLivingAttributes", at = @At(value = "RETURN"))
    private static void injectedCreateAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        ExtractAttributes.createAttrToLiving(cir.getReturnValue());
    }

    @ModifyVariable(method = "actuallyHurt", at = @At(value = "LOAD", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F", ordinal = 0), argsOnly = true)
    private float x_PlayerInfo$damageAmount(float amount, DamageSource source) {
        x_PlayerInfo$currentDamageSource = source;
        return DamageSourceUtil.additionDamage(source, amount, getMaxHealth());
    }

    @ModifyVariable(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatTracker;recordDamage(Lnet/minecraft/world/damagesource/DamageSource;F)V"), argsOnly = true)
    private float x_PlayerInfo$damageAmountHealing(float amount, DamageSource source) {
        DamageSourceUtil.setHealingAddition(source, amount);
        return amount;
    }

    @ModifyArg(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(FFF)F"), index = 1)
    private float injectedIgnoredArmorAttributes(float f) {
        return DamageSourceUtil.reductionFromArmor(x_PlayerInfo$currentDamageSource, f);
    }

    @ModifyVariable(method = "heal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"), argsOnly = true)
    private float injectedHealAttributes(float f) {
        return f * (1 + (float) getAttributeValue(ExtractAttributes.HEALING_BONUS));
    }

    @Override
    public float x_PlayerInfo$getDamageAfterMagicAbsorb(DamageSource arg, float g) {
        return getDamageAfterMagicAbsorb(arg, g);
    }

    @Override
    public void x_PlayerInfo$addDamageHealing(float healing) {
        x_PlayerInfo$lastDamageHealing += healing;
    }

    @Override
    public void x_PlayerInfo$healingPlayer() {
        if (x_PlayerInfo$lastDamageHealing >= 0.001) {
            heal(x_PlayerInfo$lastDamageHealing);
        }
    }

    @Override
    public void x_PlayerInfo$resetHealing() {
        x_PlayerInfo$lastDamageHealing = 0;
    }
}
