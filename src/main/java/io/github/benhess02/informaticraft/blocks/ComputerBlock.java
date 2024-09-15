package io.github.benhess02.informaticraft.blocks;

import io.github.benhess02.informaticraft.mspc.DisplayMessage;
import io.github.benhess02.informaticraft.Informaticraft;
import io.github.benhess02.informaticraft.mspc.KeyInputMessage;
import io.github.benhess02.informaticraft.mspc.MessageHandler;
import io.github.benhess02.informaticraft.entities.ComputerBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class ComputerBlock extends BaseEntityBlock implements MessageHandler {
    public static final MapCodec<ComputerBlock> CODEC = simpleCodec(ComputerBlock::new);

    public ComputerBlock(Properties properties) {
        super(properties);
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
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ComputerBlockEntity computerBlockEntity) {
            player.openMenu(computerBlockEntity);
        }
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
        return new ComputerBlockEntity(blockPos, blockState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void handleMessage(LevelAccessor level, BlockPos pos, BlockState state, Direction side, Object message) {
        if(message instanceof KeyInputMessage keyInputMessage) {
            Informaticraft.sendMessage(level, pos, side, new DisplayMessage(keyInputMessage.keySym));
        }
    }

    @Override
    public boolean canHandleMessage(LevelAccessor level, BlockPos pos, BlockState state, Direction side) {
        return side != state.getValue(BlockStateProperties.FACING);
    }
}
