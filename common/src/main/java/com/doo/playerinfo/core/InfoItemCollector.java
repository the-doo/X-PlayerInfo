package com.doo.playerinfo.core;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class InfoItemCollector {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, List<InfoItemServerGetter>> GETTERS = new TreeMap<>(String::compareTo);

    private static final Timer TIMER = new Timer("Collect Player Info Timer", true);

    public static void start(List<ServerPlayer> players, PacketSender sender) {
        if (sender == null) {
            LOGGER.error("Info PackSender is null");
            return;
        }
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                if (GETTERS.isEmpty()) {
                    return;
                }
                LOGGER.debug("Player Info Sync Running...Players = {}", players);

                players.forEach(player -> {
                    try {
                        InfoUpdatePacket packet = InfoUpdatePacket.create(append -> GETTERS.forEach((modName, list) -> {
                            // collect and sort
                            List<String> keys = Lists.newArrayList();
                            Map<String, List<InfoGroupItems>> info = list.stream()
                                    .flatMap(getter -> getter.get(player).stream())
                                    .peek(l -> {
                                        if (!keys.contains(l.getGroup())) {
                                            keys.add(l.getGroup());
                                        }
                                    })
                                    .collect(Collectors.groupingBy(InfoGroupItems::getGroup));

                            List<InfoGroupItems> items = InfoGroupItems.merge(keys, info);
                            append.accept(modName, items);
                        }));

                        sender.sender(player, packet);
                    } catch (Exception ex) {
                        LOGGER.warn("Send info to player {} error: ", player, ex);
                    }
                });
            }
        }, 0, 1000);

        LOGGER.debug("Player info Collector is started!");
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
}
