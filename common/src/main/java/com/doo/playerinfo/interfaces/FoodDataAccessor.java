package com.doo.playerinfo.interfaces;

import java.util.function.DoubleSupplier;

public interface FoodDataAccessor {

    static void setFoodBonus(Object player, DoubleSupplier foodBonus) {
        ((FoodDataAccessor) player).x_PlayerInfo$setExtra(foodBonus);
    }

    void x_PlayerInfo$setExtra(DoubleSupplier foodBonus);
}
