package net.pasuki.power.energy;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

public class ReceiveAndExtractEnergyStorage implements IEnergizedPowerEnergyStorage {
    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    public ReceiveAndExtractEnergyStorage(int energy, int capacity, int maxTransfer) {
        this(energy, capacity, maxTransfer, maxTransfer);
    }

    public ReceiveAndExtractEnergyStorage(int energy, int capacity, int maxReceive, int maxExtract) {
        this.energy = energy;
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            energy += energyReceived;
        }
        System.out.println("Energy received: " + energyReceived);
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            energy -= energyExtracted;
        }
        System.out.println("Energy extracted: " + energyExtracted);
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        System.out.println("Current energy stored: " + energy);
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public int getEnergy() {
        return energy;
    }

    @Override
    public void setEnergy(int energy) {
        this.energy = energy;
    }

    @Override
    public void setEnergyWithoutUpdate(int energy) {
        this.energy = energy;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public void setCapacityWithoutUpdate(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public Tag saveNBT() {
        System.out.println("Saving energy to NBT: " + energy);
        return IntTag.valueOf(energy);
    }

    @Override
    public void loadNBT(Tag tag) {
        if (tag instanceof IntTag) {
            energy = ((IntTag) tag).getAsInt();
        } else {
            energy = 0;
        }
        System.out.println("Energy loaded from NBT: " + energy);
    }
}

