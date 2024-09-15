package io.github.benhess02.informaticraft.menus;

import io.github.benhess02.informaticraft.Informaticraft;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ComputerMenu extends AbstractContainerMenu {

    protected Container container;

    public ComputerMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(1));
    }

    public ComputerMenu(int id, Inventory inventory, Container container) {
        super(Informaticraft.COMPUTER_MENU_TYPE.get(), id);
        this.container = container;
        this.addSlot(new Slot(container, 0, 44, 20));
        for(int y = 0; y < 3; ++y) {
            for(int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18 + x * 4));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 142 + i * 4));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        ItemStack result = ItemStack.EMPTY;
        Slot srcSlot = this.slots.get(i);
        if (srcSlot.hasItem()) {
            ItemStack srcItem = srcSlot.getItem();
            result = srcItem.copy();
            if (i < 1) {
                if (!this.moveItemStackTo(srcItem, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(srcItem, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (srcItem.isEmpty()) {
                srcSlot.setByPlayer(ItemStack.EMPTY);
            } else {
                srcSlot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}
