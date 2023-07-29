package com.doo.playerinfo.mixin;

import com.doo.playerinfo.interfaces.FoodDataAccessor;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

@Mixin(FoodData.class)
public abstract class FoodDataMixin implements FoodDataAccessor {

    private DoubleSupplier extraFoodBonusGetter;

    @ModifyArg(method = "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"), index = 0)
    private int injectedCreateAttributes(int i) {
        return extraFoodBonusGetter == null ? i : (int) (i * (1 + extraFoodBonusGetter.getAsDouble()));
    }

    @Override
    public void x_PlayerInfo$setExtra(DoubleSupplier foodBonus) {
        this.extraFoodBonusGetter = foodBonus;
    }
}
