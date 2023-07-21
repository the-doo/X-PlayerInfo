package com.doo.playerinfo.interfaces;

import com.doo.playerinfo.XPlayerInfo;
import com.doo.playerinfo.core.InfoGroupItems;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;

public interface OtherPlayerInfoFieldInjector {

    Map<String, List<InfoGroupItems>> playerInfo$getInfo();

    float playerInfo$getFlySpeed();

    static OtherPlayerInfoFieldInjector get(Player player) {
        return XPlayerInfo.get(player);
    }
}
