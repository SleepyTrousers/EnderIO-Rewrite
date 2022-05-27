package com.enderio.machines.common.menu;

import com.enderio.machines.common.io.item.MachineInventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class MachineSlot extends SlotItemHandler {

    public MachineSlot(MachineInventory itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public MachineInventory getItemHandler() {
        return (MachineInventory) super.getItemHandler();
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return !getItemHandler().guiExtractItem(getSlotIndex(), 1, true).isEmpty();
    }

    @Override
    @Nonnull
    public ItemStack remove(int amount) {
        return getItemHandler().guiExtractItem(getSlotIndex(), amount, false);
    }
}
