package net.pasuki.power.blocks.GeneratorBlock;

import net.pasuki.power.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class GeneratorBlockContainer extends AbstractContainerMenu {

    private final BlockPos pos;
    private int power;

    public GeneratorBlockContainer(int windowId, Player player, BlockPos pos) {
        super(Registration.GENERATOR_BLOCK_CONTAINER.get(), windowId);
        this.pos = pos;
        if (player.level().getBlockEntity(pos) instanceof GeneratorBlockEntity generator) {
            addSlot(new SlotItemHandler(generator.getItems(), GeneratorBlockEntity.SLOT, 64, 24));
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return generator.getStoredPower() & 0xffff;
                }

                @Override
                public void set(int pValue) {
                    GeneratorBlockContainer.this.power = (GeneratorBlockContainer.this.power & 0xffff0000) | (pValue & 0xffff);
                }
            });
            addDataSlot(new DataSlot() {
                @Override
                public int get() {
                    return (generator.getStoredPower() >> 16) & 0xffff;
                }

                @Override
                public void set(int pValue) {
                    GeneratorBlockContainer.this.power = (GeneratorBlockContainer.this.power & 0xffff) | ((pValue & 0xffff) << 16);
                }
            });
        }
        layoutPlayerInventorySlots(player.getInventory(), 10, 70);
    }

    public int getPower() {
        return power;
    }

    private int addSlotRange(Container playerInventory, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(new Slot(playerInventory, index, x, y));
            x += dx;
            index++;
        }
        return index;
    }

    private int addSlotBox(Container playerInventory, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            index = addSlotRange(playerInventory, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private void layoutPlayerInventorySlots(Container playerInventory, int leftCol, int topRow) {
        // Player inventory
        addSlotBox(playerInventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow += 58;
        addSlotRange(playerInventory, 0, leftCol, topRow, 9, 18);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index < GeneratorBlockEntity.SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, GeneratorBlockEntity.SLOT_COUNT, Inventory.INVENTORY_SIZE + GeneratorBlockEntity.SLOT_COUNT, true)) {
                    return ItemStack.EMPTY;
                }
            }
            if (!this.moveItemStackTo(stack, GeneratorBlockEntity.SLOT, GeneratorBlockEntity.SLOT+1, false)) {
                if (index < 27 + GeneratorBlockEntity.SLOT_COUNT) {
                    if (!this.moveItemStackTo(stack, 27 + GeneratorBlockEntity.SLOT_COUNT, 36 + GeneratorBlockEntity.SLOT_COUNT, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < Inventory.INVENTORY_SIZE + GeneratorBlockEntity.SLOT_COUNT && !this.moveItemStackTo(stack, GeneratorBlockEntity.SLOT_COUNT, 27 + GeneratorBlockEntity.SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(player.level(), pos), player, Registration.GENERATOR_BLOCK.get());
    }
}
