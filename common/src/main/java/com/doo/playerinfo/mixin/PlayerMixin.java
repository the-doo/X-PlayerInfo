package com.doo.playerinfo.mixin;

import com.doo.playerinfo.attributes.ExtractAttributes;
import com.doo.playerinfo.core.InfoGroupItems;
import com.doo.playerinfo.interfaces.OtherPlayerInfoFieldInjector;
import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(value = Player.class, priority = Integer.MAX_VALUE)
public abstract class PlayerMixin extends LivingEntity implements OtherPlayerInfoFieldInjector {

    @Unique
    private final Map<String, List<InfoGroupItems>> otherPlayerInfo = Maps.newConcurrentMap();

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyVariable(method = "giveExperiencePoints", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    private int modifyXp(int value) {
        return value < 0 ? value : (int) (value * (1 + getAttributeValue(ExtractAttributes.EX_XP)));
    }

    @Inject(method = "createAttributes", at = @At(value = "RETURN"))
    private static void injectedCreateAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue().add(ExtractAttributes.EX_XP);
    }

    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 3), ordinal = 0)
    private float injectOtherCriticalHits(float value) {
        if (level() instanceof ServerLevel && random.nextInt(0, 10000) / 100d <= getAttributeValue(ExtractAttributes.CRIT_RATE)) {
            value *= (1 + (float) (getAttributeValue(ExtractAttributes.CRIT_DAMAGE)));
        }
        return value;
    }

    @Override
    public Map<String, List<InfoGroupItems>> playerInfo$getInfo() {
        return otherPlayerInfo;
    }
}
