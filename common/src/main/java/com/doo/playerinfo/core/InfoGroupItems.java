package com.doo.playerinfo.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.IntPredicate;

public class InfoGroupItems {

    private static final String CLIENT_SIZE_FLAG = "$CLIENT_SIDE$";

    private static final DecimalFormat FORMAT = new DecimalFormat("#.###");

    private final String group;
    private final List<Pair<String, Object>> sortedItems = Lists.newArrayList();

    private InfoGroupItems(String groupName) {
        this.group = groupName;
    }

    public static InfoGroupItems group(String group) {
        return new InfoGroupItems(group);
    }

    public InfoGroupItems add(String key, Object value) {
        sortedItems.add(Pair.of(key, value));
        return this;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        CompoundTag item;
        String value;
        for (Pair<String, Object> sortedItem : sortedItems) {
            item = new CompoundTag();
            value = sortedItem.getValue().toString();
            item.putString(sortedItem.getKey(), NumberUtils.isCreatable(value) ? FORMAT.format(sortedItem.getValue()) : value);
            items.add(item);
        }
        tag.put(group, items);
        return tag;
    }

    public static InfoGroupItems fromNBT(CompoundTag tag) {
        String group = tag.getAllKeys().stream().findFirst().orElse("");
        InfoGroupItems items = InfoGroupItems.group(group);
        CompoundTag ct;
        String key;
        for (Tag t : tag.getList(group, Tag.TAG_COMPOUND)) {
            ct = (CompoundTag) t;
            key = ct.getAllKeys().stream().findFirst().orElse("");
            String value = ct.getString(key);
            if (value.equals(CLIENT_SIZE_FLAG)) {
                value = getFromClient(key).toString();
            }
            items.add(key, value);
        }
        return items;
    }

    public String getGroup() {
        return group;
    }

    public void fallbackForeach(BiConsumer<String, String> consumer, IntPredicate test, Runnable run) {
        sortedItems.forEach(i -> consumer.accept(i.getKey(), i.getValue().toString()));
        if (test.test(sortedItems.size())) {
            run.run();
        }
    }

    public InfoGroupItems addClientSideFlag(String key) {
        add(key, CLIENT_SIZE_FLAG);
        return this;
    }

    public static void addClientSideGetter(String key, InfoItemClientGetter clientGetter) {
        clientSide(key, clientGetter);
    }

    private static final Map<String, InfoItemClientGetter> CLIENT_GETTER_MAP = Maps.newHashMap();


    /**
     * Client side getter - trigger when resolve pack
     */
    public interface InfoItemClientGetter {
        Object get(Minecraft minecraft);
    }

    protected static void clientSide(String key, InfoItemClientGetter clientGetter) {
        CLIENT_GETTER_MAP.put(key, clientGetter);
    }

    protected static Object getFromClient(String key) {
        Object o = CLIENT_GETTER_MAP.containsKey(key) ? CLIENT_GETTER_MAP.get(key).get(Minecraft.getInstance()) : "";
        return NumberUtils.isCreatable((o).toString()) ? FORMAT.format(o) : o;
    }
}
