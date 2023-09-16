package com.doo.playerinfo.gui;

import com.doo.playerinfo.consts.Const;
import com.doo.playerinfo.core.InfoGroupItems;
import com.doo.playerinfo.interfaces.OtherPlayerInfoFieldInjector;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.Team;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;

import static com.doo.playerinfo.consts.Const.MINECRAFT_NAME;

@Environment(EnvType.CLIENT)
public class InfoScreen extends Screen {

    public static final KeyMapping KEY_MAPPING = new KeyMapping(
            "keybind.category.x_player_info.name",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            "keybind.key.x_player_info.name");


    private static final int START_X = 40;

    private static final int FONT_COLOR = 0XFFFFFF;

    public static final Component DAY = Component.translatable(Const.DAY);
    public static final Component NOON = Component.translatable(Const.NOON);
    public static final Component NIGHT = Component.translatable(Const.NIGHT);
    public static final Component MIDNIGHT = Component.translatable(Const.MIDNIGHT);

    private Button selected;

    private LocalPlayer player;

    private int refreshTick = 100;

    private PlayerInfo playerInfo;

    private Team team;

    public InfoScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        super.init();

        refreshTick = minecraft.getFps();
        player = minecraft.player;
        playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(player.getUUID());
        team = player.getTeam();

        Map<String, List<InfoGroupItems>> map = OtherPlayerInfoFieldInjector.get(player).playerInfo$getInfo();

        int endW = width - 115;

        InfoItemsWidget list = new InfoItemsWidget(font, 80, 35, endW, height - 60, Component.empty());
        addRenderableWidget(list);

        StringWidget text = new StringWidget(125, height - 15, 0, 9, collectTime(), font);
        Button.OnPress buttonPress = b -> {
            boolean toTop = selected != b;
            selected = b;
            list.update(font, b.getMessage(), map.get(b.getMessage().getString()), toTop);
            text.setMessage(collectTime());
        };
        TabListWidget tags = new TabListWidget(Lists.newArrayList(map.keySet()), MINECRAFT_NAME, buttonPress, font,
                82, 15, endW, 15, Component.empty());
        addRenderableWidget(tags);

        addRenderableOnly(text);

        selected = tags.getSelectedButton();
    }

    @NotNull
    private static MutableComponent collectTime() {
        return Component.literal(String.format("Collect Time: %sms",
                OtherPlayerInfoFieldInjector.get(Minecraft.getInstance().player).playerInfo$getCollectTime()));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (KEY_MAPPING.matches(i, j)) {
            minecraft.setScreen(null);
            return true;
        }

        return super.keyPressed(i, j, k);
    }

    public static void open(Minecraft client) {
        if (client.player == null) {
            return;
        }

        if (client.screen == null) {
            client.setScreen(new InfoScreen(Component.empty()));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        renderBackground(guiGraphics);

        MutableInt minY = new MutableInt(80);
        int interval = 12;

        printString(guiGraphics, player.getName(), 10);
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, 40, minY.getAndAdd(interval), 30, (float) (51) - i, (float) (75 - 50) - j, player);
        printString(guiGraphics, InfoGroupItems.FORMAT.format(player.getBbHeight()), minY.getAndAdd(interval) - 2);
        printString(guiGraphics, (minecraft.isLocalServer() ? 0 : playerInfo.getLatency()) + "ms", minY.getAndAdd(interval) - 2);

        if (playerInfo != null) {
            MutableComponent component = playerInfo.getGameMode().getShortDisplayName().copy()
                    .append(" ").append(minecraft.level.getDifficulty().getDisplayName())
                    .append(" ").append(day());
            printString(guiGraphics, component, minY.getAndAdd(interval));
        }

        if (team != null) {
            printString(guiGraphics, team.getName() + ": " + team.getPlayers().size(), minY.getAndAdd(interval));
        }

        RenderSystem.disableDepthTest();

        if (minecraft != null && refreshTick-- < 0 && selected != null) {
            minecraft.doRunTask(() -> selected.onPress());
            refreshTick = minecraft.getFps();
        }

        super.render(guiGraphics, i + 50, j + 50, f);
    }

    private Component day() {
        if (minecraft == null) {
            return DAY;
        }
        long time = minecraft.level.getDayTime() % 24000;
        if (time >= 18000) {
            return MIDNIGHT;
        } else if (time >= 13000) {
            return NIGHT;
        } else if (time >= 6000) {
            return NOON;
        } else {
            return DAY;
        }
    }

    private void printString(GuiGraphics guiGraphics, String string, int minY) {
        guiGraphics.drawCenteredString(font, string, START_X, minY, FONT_COLOR);
    }

    private void printString(GuiGraphics guiGraphics, Component component, int minY) {
        guiGraphics.drawCenteredString(font, component, START_X, minY, FONT_COLOR);
    }
}
