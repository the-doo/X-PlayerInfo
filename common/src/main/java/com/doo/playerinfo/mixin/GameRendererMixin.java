package com.doo.playerinfo.mixin;

import com.doo.playerinfo.utils.ExtractAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    /**
     * modify target is double d = this.minecraft.gameMode.getPickRange();
     */
    @ModifyVariable(method = "pick", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private double x_PlayerInfo$pickRange(double value) {
        double v = ExtractAttributes.get(minecraft.player.getAttributes(), ExtractAttributes.ATTACK_RANGE);
        if (v == 0) {
            return value;
        }

        return value + v;
    }

    /**
     * modify target is double h = vec3.distanceToSqr(vec34);
     */
    @ModifyVariable(method = "pick", at = @At(value = "STORE", ordinal = 0), ordinal = 2)
    private double x_PlayerInfo$attackRange(double value) {
        double v = ExtractAttributes.get(minecraft.player.getAttributes(), ExtractAttributes.ATTACK_RANGE);
        if (v <= 0) {
            return value;
        }

        return value - (v * v + 6 * v);
    }
}
