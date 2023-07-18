package com.doo.playerinfo.core;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.*;

public class InfoItemCollector {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static PacketSender sender;
    private static final Map<String, List<InfoItemServerGetter>> GETTERS = new TreeMap<>(String::compareTo);

    private static final Timer TIMER = new Timer("Send Player Info Timer", true);

    public static void start(MinecraftServer server) {
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                if (GETTERS.isEmpty()) {
                    return;
                }

                server.getPlayerList().getPlayers().forEach(player -> {
                    try {

                        InfoUpdatePacket packet = InfoUpdatePacket.create((consumer, finished) -> GETTERS.forEach((modName, list) -> {
                            list.forEach(getter -> getter.get(player).forEach(consumer));
                            finished.accept(modName);
                        }));

                        if (sender != null) {
                            sender.sender(player, packet);
                        }
                    } catch (Exception ex) {
                        LOGGER.warn("Player Info Sync error: ", ex);
                    }
                });
            }
        }, 0, 1000);
    }

    public interface InfoItemServerGetter {

        /**
         * return map(id -> Value)
         */
        List<InfoGroupItems> get(ServerPlayer player);
    }


    /**
     * Regis Info to screen, only to get info
     * <p>
     * if warn to sort or group by, see info_group_by.json
     */
    public static void register(String modName, InfoItemServerGetter getter) {
        GETTERS.compute(modName, (k, v) -> {
            if (v == null) {
                return Lists.newArrayList(getter);
            }
            v.add(getter);
            return v;
        });
    }

    public interface PacketSender {
        void sender(ServerPlayer player, InfoUpdatePacket packet);
    }

    public static void setSender(PacketSender sender) {
        InfoItemCollector.sender = sender;
    }
}
