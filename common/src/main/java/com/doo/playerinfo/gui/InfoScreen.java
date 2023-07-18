package com.doo.playerinfo.gui;

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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;

import static com.doo.playerinfo.consts.Const.MINECRAFT_NAME;

@Environment(EnvType.CLIENT)
public class InfoScreen extends Screen {

    public static final KeyMapping KEY_MAPPING = new KeyMapping(
            "keybind.category.player_info.name",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            "keybind.key.player_info.name");

    private Map<String, List<InfoGroupItems>> map;
    private Button selected;

    private LocalPlayer player;

    private int refreshTick = 100;

    public InfoScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        super.init();

        refreshTick = minecraft.getFps();

        player = minecraft.player;
        this.map = OtherPlayerInfoFieldInjector.get(player).playerInfo$getInfo();

        int endW = width - 115;

        InfoItemsWidget list = new InfoItemsWidget(font, 80, 35, endW, height - 60, Component.empty());
        addRenderableWidget(list);

        Button.OnPress buttonPress = b -> {
            selected = b;
            list.update(font, b.getMessage(), map.get(b.getMessage().getString()));
        };
        TabListWidget tags = new TabListWidget(Lists.newArrayList(map.keySet()), MINECRAFT_NAME, buttonPress, font,
                82, 15, endW, 15, Component.empty());
        addRenderableWidget(tags);

        selected = tags.getSelectedButton();
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

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, 40, 80, 30, (float) (51) - i, (float) (75 - 50) - j, this.minecraft.player);

        guiGraphics.drawCenteredString(font, player.getName(), 40, 100, 0XFFFFFF);

        RenderSystem.disableDepthTest();

        if (refreshTick-- < 0 && selected != null) {
            minecraft.doRunTask(() -> selected.onPress());
            refreshTick = minecraft.getFps();
        }

        super.render(guiGraphics, i + 50, j + 50, f);

        RenderSystem.enableDepthTest();
    }
}
