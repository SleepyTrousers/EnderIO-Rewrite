package com.enderio.machines.common.blockentity.base;

import com.enderio.base.EnderIO;
import com.enderio.base.common.blockentity.sync.SyncMode;
import com.enderio.base.common.capability.capacitors.ICapacitorData;
import com.enderio.base.common.util.CapacitorUtil;
import com.enderio.base.common.util.UseOnly;
import com.enderio.machines.common.MachineTier;
import com.enderio.machines.common.blockentity.data.sidecontrol.item.ItemSlotLayout;
import com.enderio.machines.common.blockentity.sync.EnergyData;
import com.enderio.machines.common.blockentity.sync.MachineEnergyDataSlot;
import com.enderio.machines.common.energy.MachineEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * A machine that stores power.
 */
public abstract class PoweredMachineEntity extends MachineBlockEntity {
    protected MachineEnergyStorage energyStorage;

    private final LazyOptional<IEnergyStorage> energyCap = LazyOptional.of(() -> this.energyStorage);

    @UseOnly(LogicalSide.CLIENT) private EnergyData clientEnergy;

    public PoweredMachineEntity(MachineTier tier, BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(tier, pType, pWorldPosition, pBlockState);

        energyStorage = createEnergyStorage();

        // Add energy storage dataslot. It will only ever be synced to the client.
        addDataSlot(new MachineEnergyDataSlot(energyStorage, vec -> clientEnergy = vec, SyncMode.GUI));
    }

    // Helper methods for gui:
    @UseOnly(LogicalSide.CLIENT)
    public EnergyData getGuiEnergy() {
        if (level.isClientSide) {
            return clientEnergy;
        }
        EnderIO.LOGGER.warn("getGuiEnergy called on server!");
        return new EnergyData(energyStorage.getEnergyStored(), energyStorage.getMaxEnergyStored());
    }

    protected MachineEnergyStorage createEnergyStorage() {
        return new MachineEnergyStorage(this::getCapacitorData) {
            @Override
            protected void onEnergyChanged() {
                setChanged();
            }
        };
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityEnergy.ENERGY) {
            return energyCap.cast(); // TODO: FUTURE: Sided access stuff.
        }
        return super.getCapability(cap, side);
    }

    // region Capacitors

    public boolean hasCapacitor() {
        return getSlotLayout()
            .flatMap(layout -> layout
                .getFirst(ItemSlotLayout.SlotType.CAPACITOR))
            .map(slot -> CapacitorUtil.isCapacitor(getItemHandler().getStackInSlot(slot)))
            .orElse(false);
    }

    public Optional<ICapacitorData> getCapacitorData() {
        return getSlotLayout().flatMap(layout -> layout
            .getFirst(ItemSlotLayout.SlotType.CAPACITOR)
            .flatMap(slot -> CapacitorUtil.getCapacitorData(getItemHandler().getStackInSlot(slot))));
    }

    // endregion
}
