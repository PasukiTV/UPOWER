package net.pasuki.power.tools;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public class AdaptedEnergyStorage implements IEnergyStorage {

    private final EnergyStorage energyStorage;

    public AdaptedEnergyStorage(EnergyStorage energyStorage) {
        this.energyStorage = energyStorage;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return energyStorage.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return energyStorage.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return energyStorage.canExtract();
    }

    @Override
    public boolean canReceive() {
        return energyStorage.canReceive();
    }
}

