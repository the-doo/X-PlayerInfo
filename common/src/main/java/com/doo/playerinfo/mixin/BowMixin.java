package com.doo.playerinfo.mixin;

import com.doo.playerinfo.attributes.ExtractAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BowItem.class)
public abstract class BowMixin {

    @ModifyVariable(method = "releaseUsing", at = @At(value = "STORE", target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F", ordinal = 0), ordinal = 0)
    private float x_PlayerInfo$bowStrengthAttr(float amount, ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        if (level.isClientSide() || !livingEntity.getAttributes().hasAttribute(ExtractAttributes.BOW_USING_SPEED)) {
            return amount;
        }

        return Math.min(1, amount * (1 + (float) livingEntity.getAttributeValue(ExtractAttributes.BOW_USING_SPEED)));
    }

    @ModifyVariable(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/AbstractArrow;shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V", ordinal = 0), ordinal = 0)
    private AbstractArrow x_PlayerInfo$arrowDamageAttr(AbstractArrow arrow, ItemStack itemStack, Level level, LivingEntity livingEntity, int i) {
        if (!livingEntity.getAttributes().hasAttribute(ExtractAttributes.BOW_DAMAGE_BONUS)) {
            return arrow;
        }

        arrow.setBaseDamage(arrow.getBaseDamage() * (1 + (float) livingEntity.getAttributeValue(ExtractAttributes.BOW_DAMAGE_BONUS)));
        return arrow;
    }
}
