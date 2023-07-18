package com.doo.playerinfo.interfaces;

import com.doo.playerinfo.core.InfoGroupItems;
import com.doo.playerinfo.utils.MixinUtil;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;

public interface OtherPlayerInfoFieldInjector {

    Map<String, List<InfoGroupItems>> playerInfo$getInfo();

    static OtherPlayerInfoFieldInjector get(Player player) {
        return MixinUtil.get(player);
    }
}
