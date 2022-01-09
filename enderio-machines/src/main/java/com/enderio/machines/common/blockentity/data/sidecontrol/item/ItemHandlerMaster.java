package com.enderio.machines.common.blockentity.data.sidecontrol.item;

import com.enderio.machines.common.blockentity.data.sidecontrol.IOConfig;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class ItemHandlerMaster extends ItemStackHandler {

    private final EnumMap<Direction, SidedItemHandlerAccess> access = new EnumMap(Direction.class);
    private final IOConfig config;

    private ItemSlotLayout layout;

    private Map<Integer, Predicate<ItemStack>> inputPredicates = new HashMap<>();
    private boolean isForceMode = false;

    public ItemHandlerMaster(IOConfig config, ItemSlotLayout layout) {
        super(layout.getSlotCount());
        this.config = config;
        this.layout = layout;
    }

    public void addPredicate(int slot, Predicate<ItemStack> predicate) {
        if (layout.isSlotType(slot, ItemSlotLayout.SlotType.OUTPUT))
            throw new IllegalArgumentException("Tried to add an insert predicate to the output slot:" + slot);
        if (slot >= getSlots())
            throw new IllegalArgumentException("Tried to add an insert predicate to an invalid slot:" + slot);
        inputPredicates.put(slot, predicate);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (layout.isSlotType(slot, ItemSlotLayout.SlotType.OUTPUT) && !isForceMode)
            return stack;
        return super.insertItem(slot, stack, simulate);
    }

    public ItemStack forceInsertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        isForceMode = true;
        ItemStack returnValue = insertItem(slot, stack, simulate);
        isForceMode = false;
        return returnValue;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return isForceMode || (inputPredicates.getOrDefault(slot, itemStack -> true).test(stack) && !layout.isSlotType(slot, ItemSlotLayout.SlotType.OUTPUT));
    }

    public ItemStack guiExtractItem(int slot, int amount, boolean simulate) {
        isForceMode = true;
        ItemStack returnValue = super.extractItem(slot, amount, simulate);
        isForceMode = false;
        return returnValue;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (layout.isSlotType(slot, ItemSlotLayout.SlotType.INPUT) && !isForceMode)
            return ItemStack.EMPTY;
        return super.extractItem(slot, amount, simulate);
    }

    public SidedItemHandlerAccess getAccess(Direction direction) {
        return access.computeIfAbsent(direction,
            dir -> new SidedItemHandlerAccess(this, dir));
    }

    public IOConfig getConfig() {
        return config;
    }

    public ItemSlotLayout getLayout() {
        return layout;
    }
}
