package com.doo.playerinfo.core;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class InfoItemCollector {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, List<InfoItemServerGetter>> GETTERS = new TreeMap<>(String::compareTo);

    private static final ScheduledExecutorService EXE = Executors.newScheduledThreadPool(10);
    private static ScheduledFuture<?> current;

    public static void start(List<ServerPlayer> players, PacketSender sender) {
        if (sender == null) {
            LOGGER.error("Info PackSender is null");
            return;
        }

        current = EXE.scheduleAtFixedRate(() -> {
            if (GETTERS.isEmpty()) {
                return;
            }
            LOGGER.debug("Player Info Sync Running...Players = {}", players);

            players.forEach(player -> EXE.execute(() -> {
                try {
                    long millis = Util.getEpochMillis();
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

                    packet.time(Util.getEpochMillis() - millis);
                    sender.sender(player, packet);
                } catch (Exception ex) {
                    LOGGER.warn("Send info to player {} error: ", player, ex);
                }
            }));
        }, 0, 1, TimeUnit.SECONDS);

        LOGGER.debug("Player info Collector is started!");
    }

    public static void clean() {
        if (current != null) {
            current.cancel(true);
        }
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
