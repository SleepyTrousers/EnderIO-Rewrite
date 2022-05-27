package com.enderio.machines.common.io.energy;

import com.enderio.api.capability.ICapacitorData;
import com.enderio.api.capability.IEnderCapabilityProvider;
import com.enderio.api.capacitor.CapacitorKey;
import com.enderio.api.energy.EnergyIOMode;
import com.enderio.api.io.IIOConfig;
import net.minecraft.core.Direction;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.EnumMap;
import java.util.function.Supplier;

/**
 * Machine energy storage medium.
 * Uses capacitor keys to determine maximum capacity and transfer rate.
 * Also provides sided access through capabilities.
 */
public class MachineEnergyStorage implements IEnergyStorage, IEnderCapabilityProvider<IEnergyStorage>, INBTSerializable<Tag> {
    private final IIOConfig config;
    private final EnergyIOMode ioMode;
    private final Supplier<ICapacitorData> capacitorData;

    private int energyStored;

    private final CapacitorKey capacityKey, transferKey;

    private final EnumMap<Direction, LazyOptional<Sided>> sideCache = new EnumMap<>(Direction.class);

    public MachineEnergyStorage(IIOConfig config, EnergyIOMode ioMode, Supplier<ICapacitorData> capacitorData, CapacitorKey capacityKey,
        CapacitorKey transferKey) {
        this.config = config;
        this.ioMode = ioMode;
        this.capacitorData = capacitorData;
        this.capacityKey = capacityKey;
        this.transferKey = transferKey;
    }

    public final IIOConfig getConfig() {
        return config;
    }

    public final EnergyIOMode getIOMode() {
        return ioMode;
    }

    // Override in BE
    protected void onContentsChanged() {

    }

    @Override
    public int getEnergyStored() {
        return Math.min(energyStored, getMaxEnergyStored());
    }

    /**
     * Set the energy stored in the storage.
     */
    public void setEnergyStored(int energy) {
        energyStored = Math.min(energy, getMaxEnergyStored());
    }

    /**
     * Add energy to the storage.
     *
     * @return The amount of energy added to the storage.
     */
    public int addEnergy(int energy) {
        int energyBefore = energyStored;
        energyStored = Math.min(energyStored + energy, getMaxEnergyStored());
        onContentsChanged();
        return energyStored - energyBefore;
    }

    /**
     * Consume energy from the storage.
     *
     * @return The amount of energy consumed from the storage.
     */
    public int consumeEnergy(int energy) {
        int energyBefore = energyStored;
        energyStored = Math.max(energyStored - energy, 0);
        onContentsChanged();
        return energyBefore - energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacityKey.getInt(capacitorData.get());
    }

    public int getMaxEnergyTransfer() {
        return transferKey.getInt(capacitorData.get());
    }

    @Override
    public boolean canExtract() {
        return getMaxEnergyTransfer() > 0 && ioMode.canOutput();
    }

    @Override
    public boolean canReceive() {
        return getMaxEnergyTransfer() > 0 && ioMode.canInput();
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;
        int energyReceived = Math.min(getMaxEnergyStored() - getEnergyStored(), Math.min(getMaxEnergyTransfer(), maxReceive));
        if (!simulate) {
            addEnergy(energyReceived);
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;
        int energyExtracted = Math.min(getEnergyStored(), Math.min(getMaxEnergyTransfer(), maxExtract));
        if (!simulate) {
            addEnergy(-energyExtracted);
        }
        return energyExtracted;
    }

    @Override
    public Capability<IEnergyStorage> getCapabilityType() {
        return CapabilityEnergy.ENERGY;
    }

    @Override
    public LazyOptional<IEnergyStorage> getCapability(Direction side) {
        if (!config.getMode(side).canConnect())
            return LazyOptional.empty();
        return sideCache.computeIfAbsent(side, dir -> LazyOptional.of(() -> new Sided(this, dir))).cast();
    }

    @Override
    public void invalidateSide(Direction side) {
        if (sideCache.containsKey(side)) {
            sideCache.get(side).invalidate();
            sideCache.remove(side);
        }
    }

    @Override
    public void invalidateCaps() {
        for (LazyOptional<Sided> side : sideCache.values()) {
            side.invalidate();
        }
    }

    @Override
    public Tag serializeNBT() {
        return IntTag.valueOf(energyStored);
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if (nbt instanceof IntTag intTag) {
            energyStored = intTag.getAsInt();
        }

        // TODO: Log error
    }

    private static class Sided implements IEnergyStorage {

        private final MachineEnergyStorage wrapped;
        private final Direction side;

        public Sided(MachineEnergyStorage wrapped, Direction side) {
            this.wrapped = wrapped;
            this.side = side;
        }

        @Override
        public int getEnergyStored() {
            return wrapped.getEnergyStored();
        }

        @Override
        public int getMaxEnergyStored() {
            return wrapped.getMaxEnergyStored();
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!canReceive())
                return 0;
            return wrapped.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!canExtract())
                return 0;
            return wrapped.extractEnergy(maxExtract, simulate);
        }

        @Override
        public boolean canExtract() {
            if (wrapped.getIOMode().respectIOConfig() && !wrapped.getConfig().getMode(side).canOutput())
                return false;
            return wrapped.canExtract();
        }

        @Override
        public boolean canReceive() {
            if (wrapped.getIOMode().respectIOConfig() && !wrapped.getConfig().getMode(side).canInput())
                return false;
            return wrapped.canReceive();
        }
    }
}
