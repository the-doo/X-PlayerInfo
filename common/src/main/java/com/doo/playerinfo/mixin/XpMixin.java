package com.doo.playerinfo.mixin;

import com.doo.playerinfo.utils.ExtractAttributes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ExperienceOrb.class)
public class XpMixin {

    @Shadow
    private int value;

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;repairPlayerItems(Lnet/minecraft/world/entity/player/Player;I)I"))
    private void modifyXp(Player player, CallbackInfo ci) {
        value = value < 1 ? value : (int) (value * (1 + ExtractAttributes.get(player.getAttributes(), ExtractAttributes.XP_BONUS)));
    }
}
