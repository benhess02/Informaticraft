package io.github.benhess02.informaticraft.client.screens;

import io.github.benhess02.informaticraft.menus.ComputerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ComputerScreen extends AbstractContainerScreen<ComputerMenu> {
    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("informaticraft", "textures/gui/computer.png");

    public ComputerScreen(ComputerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_281886_) {
        super.render(guiGraphics, mouseX, mouseY, p_281886_);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int center_x = (this.width - this.imageWidth) / 2;
        int center_y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CONTAINER_BACKGROUND, center_x, center_y, 0, 0, this.imageWidth, this.imageHeight);
    }
}
