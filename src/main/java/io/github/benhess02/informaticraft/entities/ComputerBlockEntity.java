package io.github.benhess02.informaticraft.entities;

import io.github.benhess02.informaticraft.Informaticraft;
import io.github.benhess02.informaticraft.menus.ComputerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ComputerBlockEntity extends BaseContainerBlockEntity {

    protected NonNullList<ItemStack> items;

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(Informaticraft.COMPUTER_BLOCK_ENTITY.get(), pos, state);
        items = NonNullList.withSize(1, ItemStack.EMPTY);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider holderLookup) {
        super.loadAdditional(tag, holderLookup);
        ContainerHelper.loadAllItems(tag, items, holderLookup);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider holderLookup) {
        super.saveAdditional(tag, holderLookup);
        ContainerHelper.saveAllItems(tag, items, holderLookup);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.informaticraft.computer");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return new ComputerMenu(i, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }
}
