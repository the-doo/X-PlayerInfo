package com.doo.playerinfo.utils;

import com.doo.playerinfo.core.InfoUpdatePacket;
import com.doo.playerinfo.interfaces.OtherPlayerInfoFieldInjector;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientSideHandler {

    private ClientSideHandler() {
    }

    public static void handle(InfoUpdatePacket packet) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        OtherPlayerInfoFieldInjector injector = OtherPlayerInfoFieldInjector.get(player);
        packet.handle(injector.playerInfo$getInfo());
        injector.playerInfo$setCollectTime(packet.time());
    }
}
