package com.doo.playerinfo.gui;

import com.doo.playerinfo.core.InfoGroupItems;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.List;

public class InfoItemsWidget extends AbstractScrollWidget {

    private final int itemHeight;

    private int innerHeight;

    private final List<AbstractWidget> children = new ArrayList<>();

    public InfoItemsWidget(Font font, int x, int y, int w, int h, Component component) {
        super(x, y, w, h, component);

        this.itemHeight = (int) (font.lineHeight * 2.5);
    }

    public void update(Font font, Component modName, List<InfoGroupItems> attributes) {
        children.clear();
        if (attributes == null || attributes.isEmpty()) {
            return;
        }

        int w = width / 3;
        MutableInt i = new MutableInt();
        MutableInt j = new MutableInt();
        int x = getX();
        int y = getY();
        String modNameStr = modName.getString();
        attributes.forEach(groupItems -> {
            String groupKey = modNameStr + ".info.group." + groupItems.getGroup();
            Component component = Component.translatable(groupKey);
            children.add(new StringWidget(x + i.getValue(), y + j.getValue(), width, itemHeight, component, font));
            j.add(itemHeight);

            groupItems.fallbackForeach((k, v) -> {
                Component c = Component.translatable(k).append(": ").append(v);
                Tooltip t = Tooltip.create(Component.translatableWithFallback(k + ".tip", ""));
                Button b = Button.builder(c, button -> {
                }).tooltip(t).bounds(x + i.getValue(), y + j.getValue(), w, itemHeight).build();
                children.add(b);

                if (i.addAndGet(w) > w * 2) {
                    j.add(itemHeight);
                    i.setValue(0);
                }
            }, len -> len % 3 == 0, () -> j.add(-itemHeight));
            i.setValue(0);
            j.add(itemHeight);
        });

        this.innerHeight = j.getValue();
    }

    @Override
    protected int getInnerHeight() {
        return innerHeight;
    }

    @Override
    protected double scrollRate() {
        return itemHeight;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        int x = i - getX() / 2 - 8;
        int y = j - (int) (getY() + itemHeight - scrollAmount());

        for (AbstractWidget child : children) {
            if (withinContentAreaTopBottom(getX(), child.getY())) {
                child.render(guiGraphics, x, y, f);
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        for (AbstractWidget child : children) {
            child.updateNarration(narrationElementOutput);
        }
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();
        guiGraphics.fillGradient(x, y, x + w, y + h, -804253680, -804253680);
    }
}
