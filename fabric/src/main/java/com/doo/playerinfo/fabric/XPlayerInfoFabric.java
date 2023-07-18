package com.doo.playerinfo.fabric;

import com.doo.playerinfo.XPlayerInfo;
import com.doo.playerinfo.attributes.ExtractAttributes;
import com.doo.playerinfo.consts.Const;
import com.doo.playerinfo.core.InfoItemCollector;
import com.doo.playerinfo.core.InfoUpdatePacket;
import com.doo.playerinfo.utils.InfoRegisters;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;

public class XPlayerInfoFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        XPlayerInfo.init();

        InfoRegisters.initMinecraft();

        ServerLifecycleEvents.SERVER_STARTED.register(InfoItemCollector::start);

        ExtractAttributes.initFabric();

        InfoItemCollector.setSender((player, packet) -> ServerPlayNetworking.send(player, new FabricInfoPack(packet)));
    }

    public static class FabricInfoPack implements FabricPacket {

        private final InfoUpdatePacket packet;
        public static final PacketType<FabricInfoPack> TYPE = PacketType.create(Const.CHANNEL, buf -> new FabricInfoPack(null));

        public FabricInfoPack(InfoUpdatePacket p) {
            packet = p;
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            if (packet != null) {
                packet.write(buf);
            }
        }

        @Override
        public PacketType<?> getType() {
            return TYPE;
        }
    }
}