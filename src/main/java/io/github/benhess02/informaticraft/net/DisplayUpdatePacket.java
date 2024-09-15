package io.github.benhess02.informaticraft.net;

import io.github.benhess02.informaticraft.entities.DisplayBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class DisplayUpdatePacket {
    public BlockPos blockPos;
    public CompoundTag displayState;

    public DisplayUpdatePacket(BlockPos blockPos, CompoundTag displayState) {
        this.blockPos = blockPos;
        this.displayState = displayState;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeNbt(displayState);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if(blockEntity instanceof DisplayBlockEntity displayBlockEntity) {
            displayBlockEntity.loadState(displayState);
        }
    }

    public static DisplayUpdatePacket decode(FriendlyByteBuf buf) {
        return new DisplayUpdatePacket(buf.readBlockPos(), buf.readNbt());
    }
}
