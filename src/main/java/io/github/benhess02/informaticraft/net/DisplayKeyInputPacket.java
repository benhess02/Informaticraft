package io.github.benhess02.informaticraft.net;

import io.github.benhess02.informaticraft.entities.DisplayBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class DisplayKeyInputPacket {

    public BlockPos blockPos;
    public int keySym;
    public boolean down;

    public DisplayKeyInputPacket(BlockPos blockPos, int keySym, boolean down) {
        this.blockPos = blockPos;
        this.keySym = keySym;
        this.down = down;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        buf.writeVarInt(keySym);
        buf.writeBoolean(down);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        ServerPlayer sender = ctx.getSender();
        if(sender == null) {
            return;
        }
        BlockEntity blockEntity = sender.level().getBlockEntity(blockPos);
        if(blockEntity instanceof DisplayBlockEntity displayBlockEntity) {
            displayBlockEntity.handleKey(keySym, down);
        }
    }

    public static DisplayKeyInputPacket decode(FriendlyByteBuf buf) {
        return new DisplayKeyInputPacket(buf.readBlockPos(), buf.readVarInt(), buf.readBoolean());
    }
}
