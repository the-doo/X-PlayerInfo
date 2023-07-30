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
     * modify target is double h = vec3.distanceToSqr(vec34);
     */
    @ModifyVariable(method = "pick", at = @At(value = "STORE", ordinal = 0), ordinal = 2)
    private double x_PlayerInfo$attackRange(double value) {
        if (!minecraft.player.getAttributes().hasAttribute(ExtractAttributes.ATTACK_RANGE)) {
            return value;
        }
        double attackRange = minecraft.player.getAttributeValue(ExtractAttributes.ATTACK_RANGE);
        return value - attackRange * attackRange;
    }
}
