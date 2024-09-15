package io.github.benhess02.informaticraft.entities;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.benhess02.informaticraft.mspc.DisplayMessage;
import io.github.benhess02.informaticraft.Informaticraft;
import io.github.benhess02.informaticraft.mspc.KeyInputMessage;
import io.github.benhess02.informaticraft.util.DisplayBuffer;
import io.github.benhess02.informaticraft.net.DisplayUpdatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.PacketDistributor;

import java.util.Objects;

public class DisplayBlockEntity extends BlockEntity {
    public DisplayBuffer buffer;

    public DisplayBlockEntity(BlockPos pos, BlockState state) {
        super(Informaticraft.DISPLAY_BLOCK_ENTITY.get(), pos, state);
        buffer = new DisplayBuffer(20, 20);
    }

    public void handleKey(int keySym, boolean down) {
        if(down) {
            Direction side = getBlockState().getValue(BlockStateProperties.FACING).getClockWise();
            Informaticraft.sendMessage(Objects.requireNonNull(getLevel()), getBlockPos(), side, new KeyInputMessage(keySym));
        }
    }

    public void handleMessage(Object message) {
        if(message instanceof DisplayMessage displayMessage) {
            if(displayMessage.keySym <= 127) {
                buffer.setChar(buffer.getCursorX(), buffer.getCursorY(), Character.toLowerCase((char) displayMessage.keySym));
                buffer.setForeground(buffer.getCursorX(), buffer.getCursorY(), ((255 << 16) | (255 << 8) | 255));
                buffer.setBackground(buffer.getCursorX(), buffer.getCursorY(), 0);
                if(buffer.getCursorX() == buffer.getWidth() - 1) {
                    buffer.setCursor(0, buffer.getCursorY() + 1);
                } else {
                    buffer.setCursorX(buffer.getCursorX() + 1);
                }
            } else if (displayMessage.keySym == InputConstants.KEY_LEFT) {
                buffer.setCursorX(buffer.getCursorX() - 1);
            } else if (displayMessage.keySym == InputConstants.KEY_RIGHT) {
                buffer.setCursorX(buffer.getCursorX() + 1);
            } else if (displayMessage.keySym == InputConstants.KEY_UP) {
                buffer.setCursorY(buffer.getCursorY() - 1);
            } else if (displayMessage.keySym == InputConstants.KEY_DOWN) {
                buffer.setCursorY(buffer.getCursorY() + 1);
            }
            setChanged();
            assert level != null;
            LevelChunk chunk = level.getChunkAt(getBlockPos());
            CompoundTag state = new CompoundTag();
            saveState(state);
            Informaticraft.CHANNEL.send(new DisplayUpdatePacket(getBlockPos(), state), PacketDistributor.TRACKING_CHUNK.with(chunk));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider holderProvider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, holderProvider);
        return tag;
    }

    public void saveState(CompoundTag tag) {
        tag.putInt("Width", buffer.getWidth());
        tag.putInt("Height", buffer.getHeight());
        tag.putString("Text", buffer.getTextBuffer());
        tag.putIntArray("Background", buffer.getBackgroundBuffer());
        tag.putIntArray("Foreground", buffer.getForegroundBuffer());
        tag.putInt("CursorX", buffer.getCursorX());
        tag.putInt("CursorY", buffer.getCursorY());
        tag.putBoolean("CursorVisible", buffer.isCursorVisible());
    }

    public void loadState(CompoundTag tag) {
        buffer.resize(tag.getInt("Width"), tag.getInt("Height"));
        buffer.setTextBuffer(tag.getString("Text"));
        buffer.setBackgroundBuffer(tag.getIntArray("Background"));
        buffer.setForegroundBuffer(tag.getIntArray("Foreground"));
        buffer.setCursor(tag.getInt("CursorX"), tag.getInt("CursorY"));
        buffer.setCursorVisible(tag.getBoolean("CursorVisible"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider holderProvider) {
        super.saveAdditional(tag, holderProvider);
        saveState(tag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider holderProvider) {
        super.loadAdditional(tag, holderProvider);
        loadState(tag);
    }
}
