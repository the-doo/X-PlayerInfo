package com.doo.playerinfo.mixin;

import com.doo.playerinfo.interfaces.FoodDataAccessor;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.DoubleSupplier;

@Mixin(FoodData.class)
public abstract class FoodDataMixin implements FoodDataAccessor {

    @Unique
    private DoubleSupplier extraFoodBonusGetter;

    @ModifyArg(method = {
            "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V",
            "Lnet/minecraft/world/food/FoodData;eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"), index = 0, require = 1)
    private int eatOnPlayer(int i) {
        return extraFoodBonusGetter == null ? i : (int) (i * (1 + extraFoodBonusGetter.getAsDouble()));
    }

    @Override
    public void x_PlayerInfo$setExtra(DoubleSupplier foodBonus) {
        this.extraFoodBonusGetter = foodBonus;
    }
}
