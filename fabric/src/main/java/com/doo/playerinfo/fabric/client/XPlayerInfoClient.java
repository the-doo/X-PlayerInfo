package com.doo.playerinfo.fabric.client;

import com.doo.playerinfo.consts.Const;
import com.doo.playerinfo.core.ClientSideHandler;
import com.doo.playerinfo.core.InfoGroupItems;
import com.doo.playerinfo.core.InfoUpdatePacket;
import com.doo.playerinfo.gui.InfoScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

@Environment(EnvType.CLIENT)
public class XPlayerInfoClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(InfoScreen.KEY_MAPPING);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (InfoScreen.KEY_MAPPING.consumeClick()) {
                InfoScreen.open(client);
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(Const.CHANNEL, (client, handler, buf, responseSender) -> {
            ClientSideHandler.handle(new InfoUpdatePacket(buf));
        });

        InfoGroupItems.addClientSideGetter(Const.PICK_RANGE, minecraft -> minecraft.gameMode.getPickRange());
    }
}
