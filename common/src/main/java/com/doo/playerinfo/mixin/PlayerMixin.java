package com.doo.playerinfo.mixin;

import com.doo.playerinfo.XPlayerInfo;
import com.doo.playerinfo.core.InfoGroupItems;
import com.doo.playerinfo.interfaces.FoodDataAccessor;
import com.doo.playerinfo.interfaces.OtherPlayerInfoFieldInjector;
import com.doo.playerinfo.utils.DamageSourceUtil;
import com.doo.playerinfo.utils.ExtractAttributes;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(value = Player.class, priority = Integer.MAX_VALUE)
public abstract class PlayerMixin extends LivingEntity implements OtherPlayerInfoFieldInjector {

    @Shadow
    protected abstract float getFlyingSpeed();

    @Shadow
    public abstract FoodData getFoodData();

    @Unique
    private final Map<String, List<InfoGroupItems>> x_PlayerInfo$otherPlayerInfo = Maps.newConcurrentMap();

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void initSetFoodBonus(Level level, BlockPos blockPos, float f, GameProfile gameProfile, CallbackInfo ci) {
        if (level.isClientSide()) {
            return;
        }
        FoodDataAccessor.setFoodBonus(getFoodData(), () -> ExtractAttributes.get(getAttributes(), ExtractAttributes.FOOD_BONUS));
    }

    @ModifyVariable(method = "giveExperiencePoints", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    private int modifyXp(int value) {
        return value <= 0 ? value : (int) (value * (1 + ExtractAttributes.get(getAttributes(), ExtractAttributes.XP_BONUS)));
    }

    @ModifyVariable(method = "setAbsorptionAmount", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getEntityData()Lnet/minecraft/network/syncher/SynchedEntityData;"), ordinal = 0, argsOnly = true)
    private float modifyAbsorptionBonus(float value) {
        return value <= 0 ? value : (int) (value * (1 + ExtractAttributes.get(getAttributes(), ExtractAttributes.ABSORPTION_BONUS)));
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;doPostDamageEffects(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;)V"))
    private void injectedAttackHealing(Entity entity, CallbackInfo ci) {
        DamageSourceUtil.healingIfPlayerHasAttr(XPlayerInfo.get(this));
    }

    @Inject(method = "createAttributes", at = @At(value = "RETURN"))
    private static void injectedCreateAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        ExtractAttributes.createAttrToPlayer(cir.getReturnValue());
    }

    @ModifyArg(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"), index = 1)
    private AABB injectedTickItemsBox(AABB box) {
        ExtractAttributes.playerTouchItems(XPlayerInfo.get(this), box);
        return box;
    }

    @Override
    public Map<String, List<InfoGroupItems>> playerInfo$getInfo() {
        return x_PlayerInfo$otherPlayerInfo;
    }

    @Override
    public float playerInfo$getFlySpeed() {
        return getFlyingSpeed();
    }
}
