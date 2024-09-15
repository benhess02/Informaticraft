package io.github.benhess02.informaticraft.net;

import io.github.benhess02.informaticraft.entities.DisplayBlockEntity;
import io.github.benhess02.informaticraft.client.screens.DisplayScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class DisplayOpenPacket {
    public BlockPos blockPos;

    public DisplayOpenPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if(blockEntity instanceof DisplayBlockEntity displayBlockEntity) {
            Minecraft.getInstance().setScreen(new DisplayScreen(displayBlockEntity));
        }
    }

    public static DisplayOpenPacket decode(FriendlyByteBuf buf) {
        return new DisplayOpenPacket(buf.readBlockPos());
    }
}
