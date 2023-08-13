package com.doo.playerinfo.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.List;

/**
 * Tab list widget
 */
public class TabListWidget extends AbstractWidget {

    private final List<PlainTextButton> buttons = Lists.newArrayList();
    private Button selectedButton;

    private final MutableInt offset = new MutableInt();

    private final int minX;
    private final int widX;

    private final int limit;

    private final PlainTextButton prev;
    private final PlainTextButton next;

    public TabListWidget(List<String> keys, String selected, Button.OnPress press, Font font, int x, int y, int w, int h, Component component) {
        super(x, y, w, h, component);

        int prevW = 0;
        int ww;
        PlainTextButton button;
        for (String key : keys) {
            ww = font.width(key);
            button = new PlainTextButton(x + prevW, y, ww, h, Component.literal(key), b -> {
                if (selectedButton != null) {
                    selectedButton.setFocused(false);
                }
                b.setFocused(true);
                selectedButton = b;
                press.onPress(b);
            }, font) {
                @Override
                public int getX() {
                    // support slide
                    return super.getX() + offset.getValue();
                }
            };

            if (selected.equals(key)) {
                this.selectedButton = button;
            }
            this.buttons.add(button);
            prevW += ww + 10;
        }

        minX = getX();
        widX = minX + width;
        limit = -(prevW - 10 - width);

        prev = new PlainTextButton(x - 10, y, 4, h, Component.literal("<"), b -> {
            offset.add(30);
            if (offset.getValue() > 0) {
                offset.setValue(0);
            }
        }, font);
        next = new PlainTextButton(x + w + 5, y, 4, h, Component.literal(">"), b -> {
            offset.add(-30);
            if (offset.getValue() < limit) {
                offset.setValue(limit);
            }
        }, font);

        if (selectedButton != null) {
            selectedButton.onPress();
        }
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        prev.visible = false;
        next.visible = false;
        if (offset.getValue() < 0) {
            prev.renderWidget(guiGraphics, i, j, f);
            prev.visible = true;
        }

        guiGraphics.enableScissor(this.getX() - 1, this.getY() - 1, this.getX() + this.width - 1, this.getY() + this.height - 1);
        for (PlainTextButton button : buttons) {
            if (button.getX() + button.getWidth() < minX || button.getX() > widX) {
                continue;
            }

            button.renderWidget(guiGraphics, i, j, f);
        }
        guiGraphics.disableScissor();

        if (offset.getValue() > limit) {
            next.renderWidget(guiGraphics, i, j, f);
            next.visible = true;
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        selectedButton.updateWidgetNarration(narrationElementOutput);
    }

    @Override
    protected boolean clicked(double d, double e) {
        if (next.visible && next.isMouseOver(d, e)) {
            next.onClick(d, e);
            return true;
        }
        if (prev.visible && prev.isMouseOver(d, e)) {
            prev.onClick(d, e);
            return true;
        }

        if (!isMouseOver(d, e)) {
            return false;
        }

        for (PlainTextButton button : buttons) {
            if (button.isMouseOver(d, e)) {
                button.onClick(d, e);
                return true;
            }
        }
        return false;
    }

    public Button getSelectedButton() {
        return selectedButton;
    }
}
