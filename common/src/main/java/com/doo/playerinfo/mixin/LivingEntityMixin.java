package com.doo.playerinfo.mixin;

import com.doo.playerinfo.XPlayerInfo;
import com.doo.playerinfo.interfaces.LivingEntityAccessor;
import com.doo.playerinfo.utils.DamageSourceUtil;
import com.doo.playerinfo.utils.ExtractAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityAccessor {

    protected LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract double getAttributeValue(Attribute attribute);

    @Shadow
    public abstract float getMaxHealth();

    @Shadow
    protected abstract float getDamageAfterMagicAbsorb(DamageSource damageSource, float f);


    @Shadow
    protected abstract void actuallyHurt(DamageSource damageSource, float f);

    @Shadow
    public abstract void heal(float f);

    @Shadow
    protected abstract float getJumpPower();

    @Shadow
    public abstract AttributeMap getAttributes();

    @Shadow
    protected abstract void jumpFromGround();

    @Shadow
    private int noJumpDelay;
    @Shadow
    protected boolean jumping;

    @Shadow
    protected abstract boolean isAffectedByFluids();

    @Shadow
    public abstract boolean isAlive();

    @Unique
    private DamageSource x_PlayerInfo$currentDamageSource;
    @Unique
    private float x_PlayerInfo$lastDamageHealing;
    @Unique
    private int x_PlayerInfo$usedJumpCount;

    @Inject(method = "createLivingAttributes", at = @At(value = "RETURN"))
    private static void injectedCreateAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        ExtractAttributes.createAttrToLiving(cir.getReturnValue());
    }

    @Inject(method = "tickEffects", at = @At(value = "HEAD"))
    private void injectedEndTick(CallbackInfo ci) {
        if (level().isClientSide() || !isAlive()) {
            return;
        }

        LivingEntity entity = XPlayerInfo.get(this);
        double v;
        if (entity.tickCount % 10 == 0 && (v = ExtractAttributes.get(getAttributes(), ExtractAttributes.HEALING_PER_BONUS)) > 0) {
            heal((float) (v / 2));
        }
    }

    @Inject(method = "getJumpPower", at = @At(value = "RETURN"), cancellable = true)
    private void injectedGetJumpPower(CallbackInfoReturnable<Float> cir) {
        double v = ExtractAttributes.get(getAttributes(), ExtractAttributes.JUMP_STRENGTH_BONUS);
        if (v != 0) {
            cir.setReturnValue((float) (cir.getReturnValue() * (1 + v)));
        }
    }

    @ModifyVariable(method = "actuallyHurt", at = @At(value = "LOAD", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F", ordinal = 0), argsOnly = true)
    private float modifyVariableDamageAmount(float amount, DamageSource source) {
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
        if (!getAttributes().hasAttribute(ExtractAttributes.HEALING_BONUS)) {
            return f;
        }

        return f * (1 + (float) getAttributeValue(ExtractAttributes.HEALING_BONUS));
    }

    @Inject(method = "canBeAffected", at = @At(value = "HEAD"), cancellable = true, require = 1)
    private void testIgnoredSetAbsorptionAmount(MobEffectInstance mobEffectInstance, CallbackInfoReturnable<Boolean> cir) {
        if (DamageSourceUtil.isTest()) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 2))
    private void jumpAttach(CallbackInfo ci) {
        if (!getAttributes().hasAttribute(ExtractAttributes.JUMP_COUNT)) {
            x_PlayerInfo$usedJumpCount = 0;
            return;
        }

        if (this.jumping && this.isAffectedByFluids()) {
            if (noJumpDelay == 0 && !onGround() && getAttributeValue(ExtractAttributes.JUMP_COUNT) > x_PlayerInfo$usedJumpCount) {
                jumpFromGround();
                noJumpDelay = 10;
                x_PlayerInfo$usedJumpCount++;
            }
        } else if (onGround() && noJumpDelay <= 0) {
            x_PlayerInfo$usedJumpCount = 0;
        }
    }

    @Override
    public void x_PlayerInfo$actuallyHurt(DamageSource arg, float g) {
        actuallyHurt(arg, g);
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

    @Override
    public float x_PlayerInfo$getJumpPower() {
        return getJumpPower();
    }

    @Override
    public float x_PlayerInfo$getDamageAfterMagicAbsorb(DamageSource arg, float g) {
        return getDamageAfterMagicAbsorb(arg, g);
    }
}
