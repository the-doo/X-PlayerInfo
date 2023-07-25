package com.doo.playerinfo.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
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
    private final boolean isKey;
    private AttributeMap attributes;
    private final List<Pair<String, Object>> sortedItems = Lists.newArrayList();

    private InfoGroupItems(String group, boolean isKey) {
        this.group = group;
        this.isKey = isKey;
    }

    public static InfoGroupItems group(String group) {
        return new InfoGroupItems(group, false);
    }

    public static InfoGroupItems groupKey(String group) {
        return new InfoGroupItems(group, true);
    }

    public InfoGroupItems attrMap(AttributeMap attributes) {
        this.attributes = attributes;
        return this;
    }

    public InfoGroupItems add(String key, Object value, boolean isPercentage) {
        if (isPercentage && value instanceof Number n) {
            sortedItems.add(Pair.of(key, FORMAT.format(n.doubleValue() * 100) + "%"));
        } else {
            sortedItems.add(Pair.of(key, value));
        }
        return this;
    }

    public InfoGroupItems addAttr(Attribute attribute, boolean isPercentage) {
        if (!attributes.hasAttribute(attribute)) {
            add(attribute.getDescriptionId(), 0, isPercentage);
            return this;
        }
        add(attribute.getDescriptionId(), attributes.getValue(attribute), isPercentage);
        return this;
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag items = new ListTag();
        CompoundTag item = new CompoundTag();
        item.putBoolean("isKey", isKey);
        items.add(item);
        for (Pair<String, Object> sortedItem : sortedItems) {
            item = new CompoundTag();
            Object v = sortedItem.getValue();
            if (v instanceof String) {
                item.putString(sortedItem.getKey(), v.toString());
            } else if (v instanceof Number) {
                item.putString(sortedItem.getKey(), FORMAT.format(v));
            } else if (v instanceof Boolean b) {
                item.putBoolean(sortedItem.getKey(), b);
            }
            items.add(item);
        }
        tag.put(group, items);
        return tag;
    }

    public static InfoGroupItems fromNBT(CompoundTag tag) {
        String group = tag.getAllKeys().stream().findFirst().orElse("");
        ListTag list = tag.getList(group, Tag.TAG_COMPOUND);
        boolean isKey = ((CompoundTag) list.remove(0)).getBoolean("isKey");
        InfoGroupItems items = isKey ? InfoGroupItems.groupKey(group) : InfoGroupItems.group(group);
        CompoundTag ct;
        String key;
        for (Tag t : list) {
            ct = (CompoundTag) t;
            key = ct.getAllKeys().stream().findFirst().orElse("");
            Object value = ct.getString(key);
            if (value.equals(CLIENT_SIZE_FLAG)) {
                value = getFromClient(key).toString();
            } else if (((String) value).isEmpty()) {
                value = ct.getBoolean(key);
            }
            items.add(key, value, false);
        }
        return items;
    }

    public String getGroup(String prefix) {
        return isKey ? group : prefix + group;
    }

    public boolean isEmpty() {
        return sortedItems.isEmpty();
    }

    public void fallbackForeach(BiConsumer<String, String> consumer, IntPredicate test, Runnable run) {
        sortedItems.forEach(i -> consumer.accept(i.getKey(), i.getValue().toString()));
        if (test.test(sortedItems.size())) {
            run.run();
        }
    }

    public InfoGroupItems addClientSideFlag(String key) {
        add(key, CLIENT_SIZE_FLAG, false);
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
