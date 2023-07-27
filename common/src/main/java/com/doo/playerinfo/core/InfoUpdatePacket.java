package com.doo.playerinfo.core;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The Packet is Update Player Info
 */
public class InfoUpdatePacket {
    private final CompoundTag nbt;

    private InfoUpdatePacket() {
        this.nbt = new CompoundTag();
    }

    public InfoUpdatePacket(FriendlyByteBuf buf) {
        nbt = buf.readNbt();
    }

    static InfoUpdatePacket create(BiConsumer<Consumer<InfoGroupItems>, Consumer<String>> builder) {
        InfoUpdatePacket packet = new InfoUpdatePacket();
        Mutable<ListTag> items = new MutableObject<>(new ListTag());
        builder.accept(
                // item foreach
                group -> items.getValue().add(group.toNBT()),
                // mod collect finish
                modName -> {
                    packet.nbt.put(modName, items.getValue());
                    items.setValue(new ListTag());
                });
        return packet;
    }

    public static InfoUpdatePacket create(Consumer<BiConsumer<String, List<InfoGroupItems>>> builder) {
        InfoUpdatePacket packet = new InfoUpdatePacket();
        builder.accept((modName, list) -> {
            ListTag tags = new ListTag();
            list.forEach(i -> tags.add(i.toNBT()));
            packet.nbt.put(modName, tags);
        });
        return packet;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(nbt);
    }

    public void handle(Map<String, List<InfoGroupItems>> map) {
        nbt.getAllKeys().forEach(modName -> {
            List<InfoGroupItems> items = Lists.newArrayList();
            for (Tag tag : nbt.getList(modName, Tag.TAG_COMPOUND)) {
                items.add(InfoGroupItems.fromNBT((CompoundTag) tag));
            }
            map.put(modName, items);
        });
    }
}
