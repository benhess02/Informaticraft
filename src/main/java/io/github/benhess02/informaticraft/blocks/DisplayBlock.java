package io.github.benhess02.informaticraft.blocks;

import io.github.benhess02.informaticraft.Informaticraft;
import io.github.benhess02.informaticraft.mspc.MessageHandler;
import io.github.benhess02.informaticraft.entities.DisplayBlockEntity;
import com.mojang.serialization.MapCodec;
import io.github.benhess02.informaticraft.net.DisplayOpenPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.PacketDistributor;

public class DisplayBlock extends BaseEntityBlock implements MessageHandler {
    public static final MapCodec<DisplayBlock> CODEC = simpleCodec(DisplayBlock::new);

    public DisplayBlock(Properties p_49224_) {
        super(p_49224_);
        registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Informaticraft.CHANNEL.send(new DisplayOpenPacket(pos), PacketDistributor.PLAYER.with((ServerPlayer) player));
        return InteractionResult.CONSUME;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(BlockStateProperties.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new DisplayBlockEntity(blockPos, blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void handleMessage(LevelAccessor level, BlockPos pos, BlockState state, Direction side, Object message) {
        if(level.getBlockEntity(pos) instanceof DisplayBlockEntity displayBlockEntity) {
            displayBlockEntity.handleMessage(message);
        }
    }

    @Override
    public boolean canHandleMessage(LevelAccessor level, BlockPos pos, BlockState state, Direction side) {
        return side == state.getValue(BlockStateProperties.FACING).getClockWise();
    }
}
