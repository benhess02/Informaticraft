package io.github.benhess02.informaticraft;

import io.github.benhess02.informaticraft.blocks.ComputerBlock;
import io.github.benhess02.informaticraft.blocks.DisplayBlock;
import io.github.benhess02.informaticraft.blocks.WireBlock;
import io.github.benhess02.informaticraft.client.renderers.DisplayRenderer;
import io.github.benhess02.informaticraft.entities.ComputerBlockEntity;
import io.github.benhess02.informaticraft.entities.DisplayBlockEntity;
import io.github.benhess02.informaticraft.menus.ComputerMenu;
import io.github.benhess02.informaticraft.client.screens.ComputerScreen;
import io.github.benhess02.informaticraft.mspc.MessageHandler;
import io.github.benhess02.informaticraft.net.DisplayKeyInputPacket;
import io.github.benhess02.informaticraft.net.DisplayOpenPacket;
import io.github.benhess02.informaticraft.net.DisplayUpdatePacket;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(Informaticraft.MODID)
public class Informaticraft
{
    public static final String MODID = "informaticraft";

    public static final SimpleChannel CHANNEL = ChannelBuilder.named(new ResourceLocation("informaticraft", "main")).simpleChannel();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<Block> COMPUTER_BLOCK = BLOCKS.register("computer",
            () -> new ComputerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    public static final RegistryObject<Block> DISPLAY_BLOCK = BLOCKS.register("display",
            () -> new DisplayBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    public static final RegistryObject<Block> WIRE_BLOCK = BLOCKS.register("wire",
            () -> new WireBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .isViewBlocking((blockState, blockGetter, blockPos) -> false)));

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> COMPUTER_BLOCK_ITEM = ITEMS.register("computer",
            () -> new BlockItem(COMPUTER_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> DISPLAY_BLOCK_ITEM = ITEMS.register("display",
            () -> new BlockItem(DISPLAY_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<Item> WIRE_BLOCK_ITEM = ITEMS.register("wire",
            () -> new BlockItem(WIRE_BLOCK.get(), new Item.Properties()));

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final RegistryObject<BlockEntityType<ComputerBlockEntity>> COMPUTER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("computer",
            () -> BlockEntityType.Builder.of(ComputerBlockEntity::new, COMPUTER_BLOCK.get()).build(null));
    public static final RegistryObject<BlockEntityType<DisplayBlockEntity>> DISPLAY_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("display",
            () -> BlockEntityType.Builder.of(DisplayBlockEntity::new, DISPLAY_BLOCK.get()).build(null));

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("informaticraft", () -> CreativeModeTab.builder()
            .icon(() -> COMPUTER_BLOCK_ITEM.get().getDefaultInstance())
            .title(Component.translatable("tab.informaticraft.informaticraft_tab"))
            .displayItems((parameters, output) -> {
                output.accept(COMPUTER_BLOCK_ITEM.get());
                output.accept(DISPLAY_BLOCK_ITEM.get());
                output.accept(WIRE_BLOCK_ITEM.get());
            }).build());

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final RegistryObject<MenuType<ComputerMenu>> COMPUTER_MENU_TYPE = MENU_TYPES.register("computer_menu",
            () -> new MenuType<>(ComputerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public Informaticraft()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::clientSetup);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MENU_TYPES.register(modEventBus);

        CHANNEL.messageBuilder(DisplayKeyInputPacket.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DisplayKeyInputPacket::encode)
                .decoder(DisplayKeyInputPacket::decode)
                .consumerMainThread(DisplayKeyInputPacket::handle)
                .add();

        CHANNEL.messageBuilder(DisplayOpenPacket.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DisplayOpenPacket::encode)
                .decoder(DisplayOpenPacket::decode)
                .consumerMainThread(DisplayOpenPacket::handle)
                .add();

        CHANNEL.messageBuilder(DisplayUpdatePacket.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DisplayUpdatePacket::encode)
                .decoder(DisplayUpdatePacket::decode)
                .consumerMainThread(DisplayUpdatePacket::handle)
                .add();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

    }

    public void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(COMPUTER_MENU_TYPE.get(), ComputerScreen::new);

            BlockEntityRenderers.register(DISPLAY_BLOCK_ENTITY.get(), DisplayRenderer::new);
        });
    }

    public static boolean sendMessage(LevelAccessor level, BlockPos source, Direction outSide, Object message) {
        BlockPos pos = source.relative(outSide);
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof MessageHandler handler) {
            Direction side = outSide.getOpposite();
            if(handler.canHandleMessage(level, pos, state, side)) {
                handler.handleMessage(level, pos, state, side, message);
                return true;
            }
        }
        return false;
    }

    public static boolean canSendMessage(LevelAccessor level, BlockPos source, Direction outSide) {
        BlockPos pos = source.relative(outSide);
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof MessageHandler handler) {
            return handler.canHandleMessage(level, pos, state, outSide.getOpposite());
        }
        return false;
    }
}
