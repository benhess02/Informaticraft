package io.github.benhess02.informaticraft.mspc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface MessageHandler {
    void handleMessage(LevelAccessor level, BlockPos pos, BlockState state, Direction side, Object message);
    boolean canHandleMessage(LevelAccessor level, BlockPos pos, BlockState state, Direction side);
}
