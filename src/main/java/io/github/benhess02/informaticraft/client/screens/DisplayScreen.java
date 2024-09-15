package io.github.benhess02.informaticraft.client.screens;

import io.github.benhess02.informaticraft.util.DisplayBuffer;
import io.github.benhess02.informaticraft.Informaticraft;
import io.github.benhess02.informaticraft.entities.DisplayBlockEntity;
import io.github.benhess02.informaticraft.net.DisplayKeyInputPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.PacketDistributor;

public class DisplayScreen extends Screen {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("informaticraft", "textures/gui/display.png");

    public DisplayBlockEntity displayEntity;

    public Font font;

    public DisplayScreen(DisplayBlockEntity displayEntity) {
        super(Component.translatable("container.informaticraft.display"));
        this.displayEntity = displayEntity;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int p_281550_, int p_282878_, float p_282465_) {
        super.renderTransparentBackground(graphics);
        int imageWidth = 256;
        int imageHeight = 144;
        int corner_x = (this.width - imageWidth) / 2;
        int corner_y = (this.height - imageHeight) / 2;
        graphics.blit(BACKGROUND, corner_x, corner_y, 0, 0, imageWidth, imageHeight);
        DisplayBuffer buffer = displayEntity.buffer;
        for(int y = 0; y < buffer.getHeight(); y++) {
            for(int x = 0; x < buffer.getWidth(); x++) {
                int letter_x = corner_x + 8 * x;
                int letter_y = corner_y + 8 * y;
                int background;
                int foreground;
                if(x == buffer.getCursorX() && y == buffer.getCursorY()) {
                    background = (255 << 16) | (255 << 8) | 255;
                    foreground = 0;
                } else {
                    background = buffer.getBackground(x, y);
                    foreground = buffer.getForeground(x, y);
                }
                graphics.fill(letter_x, letter_y, letter_x + 8, letter_y + 8, (255 << 24) | background);
                graphics.drawString(this.font, Character.toString(buffer.getChar(x, y)), letter_x, letter_y, foreground, false);
            }
        }
    }

    @Override
    public boolean keyPressed(int keysym, int scancode, int p_96554_) {
        if (keysym == InputConstants.KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        Informaticraft.CHANNEL.send(
                new DisplayKeyInputPacket(displayEntity.getBlockPos(), keysym, true),
                PacketDistributor.SERVER.noArg()
        );
        return true;
    }

    @Override
    public boolean keyReleased(int keysym, int scancode, int p_94717_) {
        Informaticraft.CHANNEL.send(
                new DisplayKeyInputPacket(displayEntity.getBlockPos(), keysym, false),
                PacketDistributor.SERVER.noArg()
        );
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
