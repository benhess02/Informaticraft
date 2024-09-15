package io.github.benhess02.informaticraft.blocks;

import io.github.benhess02.informaticraft.Informaticraft;
import io.github.benhess02.informaticraft.mspc.MessageHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

public class WireBlock extends Block implements MessageHandler {
    private static final Property<WireSideState> NORTH = EnumProperty.create("north", WireSideState.class);
    private static final Property<WireSideState> EAST = EnumProperty.create("east", WireSideState.class);
    private static final Property<WireSideState> SOUTH = EnumProperty.create("south", WireSideState.class);
    private static final Property<WireSideState> WEST = EnumProperty.create("west", WireSideState.class);
    private static final Property<WireSideState> UP = EnumProperty.create("up", WireSideState.class);
    private static final Property<WireSideState> DOWN = EnumProperty.create("down", WireSideState.class);

    private static final Map<Direction, Property<WireSideState>> DIRECTION_PROPERTY_MAP = Map.of(
            Direction.NORTH, NORTH,
            Direction.EAST, EAST,
            Direction.SOUTH, SOUTH,
            Direction.WEST, WEST,
            Direction.UP, UP,
            Direction.DOWN, DOWN
    );

    public WireBlock(Properties properties) {
        super(properties);
        registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, WireSideState.NONE)
                .setValue(EAST, WireSideState.NONE)
                .setValue(SOUTH, WireSideState.NONE)
                .setValue(WEST, WireSideState.NONE)
                .setValue(UP, WireSideState.NONE)
                .setValue(DOWN, WireSideState.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> stateBuilder) {
        stateBuilder.add(NORTH);
        stateBuilder.add(EAST);
        stateBuilder.add(SOUTH);
        stateBuilder.add(WEST);
        stateBuilder.add(UP);
        stateBuilder.add(DOWN);
    }

    BlockState calculateBlockState(BlockState state, LevelAccessor level, BlockPos pos) {
        for(Map.Entry<Direction, Property<WireSideState>> entry : DIRECTION_PROPERTY_MAP.entrySet()) {
            if(Informaticraft.canSendMessage(level, pos, entry.getKey())) {
                Block destBlock = level.getBlockState(pos.relative(entry.getKey())).getBlock();
                if(destBlock instanceof WireBlock) {
                    state = state.setValue(entry.getValue(), WireSideState.WIRE);
                } else {
                    state = state.setValue(entry.getValue(), WireSideState.PLUG);
                }
            } else{
                state = state.setValue(entry.getValue(), WireSideState.NONE);
            }
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState otherState, LevelAccessor level, BlockPos pos, BlockPos otherPos) {
        return calculateBlockState(state, level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter p_60556_, BlockPos p_60557_, CollisionContext p_60558_) {
        VoxelShape result = Block.box(6, 6, 6, 10, 10, 10);
        if(state.getValue(NORTH) != WireSideState.NONE) {
            result = Shapes.or(result, Block.box(6, 6, 0, 10, 10, 6));
        }
        if(state.getValue(SOUTH) != WireSideState.NONE) {
            result = Shapes.or(result, Block.box(6, 6, 10, 10, 10, 16));
        }
        if(state.getValue(EAST) != WireSideState.NONE) {
            result = Shapes.or(result, Block.box(10, 6, 6, 16, 10, 10));
        }
        if(state.getValue(WEST) != WireSideState.NONE) {
            result = Shapes.or(result, Block.box(0, 6, 6, 6, 10, 10));
        }
        if(state.getValue(UP) != WireSideState.NONE) {
            result = Shapes.or(result, Block.box(6, 10, 6, 10, 16, 10));
        }
        if(state.getValue(DOWN) != WireSideState.NONE) {
            result = Shapes.or(result, Block.box(6, 0, 6, 10, 6, 10));
        }
        return result;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return calculateBlockState(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    public void handleMessage(LevelAccessor level, BlockPos pos, BlockState state, Direction side, Object message) {
        for(Map.Entry<Direction, Property<WireSideState>> entry : DIRECTION_PROPERTY_MAP.entrySet()) {
            if(entry.getKey() != side && state.getValue(entry.getValue()) != WireSideState.NONE) {
                Informaticraft.sendMessage(level, pos, entry.getKey(), message);
            }
        }
    }

    @Override
    public boolean canHandleMessage(LevelAccessor level, BlockPos pos, BlockState state, Direction side) {
        return true;
    }
}
