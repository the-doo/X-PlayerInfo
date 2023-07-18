package com.doo.playerinfo.mixin;

import com.doo.playerinfo.core.InfoGroupItems;
import com.doo.playerinfo.interfaces.OtherPlayerInfoFieldInjector;
import com.google.common.collect.Maps;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.Map;

@Mixin(value = LocalPlayer.class)
public abstract class LocalPlayerMixin implements OtherPlayerInfoFieldInjector {

    @Unique
    private final Map<String, List<InfoGroupItems>> x_PlayerInfo$otherPlayerInfo = Maps.newConcurrentMap();

    @Override
    public Map<String, List<InfoGroupItems>> playerInfo$getInfo() {
        return x_PlayerInfo$otherPlayerInfo;
    }
}
