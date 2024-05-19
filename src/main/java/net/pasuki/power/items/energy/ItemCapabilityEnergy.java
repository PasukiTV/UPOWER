package net.pasuki.power.items.energy;

import net.pasuki.power.energy.IEnergizedPowerEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemCapabilityEnergy implements ICapabilityProvider, IEnergyStorage {
    private final ItemStack itemStack; // Der ItemStack, zu dem diese Energie-Fähigkeit gehört
    private final IEnergizedPowerEnergyStorage energyStorage; // Das Energie-Speicherobjekt
    private final LazyOptional<IEnergyStorage> lazyEnergyStorage; // LazyOptional zur Bereitstellung der Energie-Fähigkeit

    // Konstruktor der Klasse
    public ItemCapabilityEnergy(ItemStack itemStack, @Nullable CompoundTag nbt, IEnergizedPowerEnergyStorage energyStorage) {
        this.itemStack = itemStack;
        this.energyStorage = energyStorage;

        // Falls ein NBT-Tag mit Energieinformationen vorhanden ist, wird der Energie-Speicher geladen
        if(nbt != null && nbt.contains("energy"))
            this.energyStorage.loadNBT(nbt.get("energy"));

        // Initialisiert das LazyOptional-Objekt für die Energie-Fähigkeit
        lazyEnergyStorage = LazyOptional.of(() -> this);
    }

    // Gibt die Energie-Fähigkeit zurück, wenn danach gefragt wird
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == ForgeCapabilities.ENERGY) {
            return lazyEnergyStorage.cast(); // Wandelt das LazyOptional in den geforderten Typ um
        }

        return LazyOptional.empty(); // Gibt ein leeres LazyOptional zurück, wenn die Fähigkeit nicht unterstützt wird
    }

    // Methode zum Empfangen von Energie
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int ret = energyStorage.receiveEnergy(maxReceive, simulate);

        // Wenn es keine Simulation ist, wird der aktuelle Energiezustand im NBT-Tag des ItemStacks gespeichert
        if(!simulate)
            itemStack.getOrCreateTag().put("energy", energyStorage.saveNBT());

        return ret; // Gibt die tatsächlich empfangene Energiemenge zurück
    }

    // Methode zum Extrahieren von Energie
    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int ret = energyStorage.extractEnergy(maxExtract, simulate);

        // Wenn es keine Simulation ist, wird der aktuelle Energiezustand im NBT-Tag des ItemStacks gespeichert
        if(!simulate)
            itemStack.getOrCreateTag().put("energy", energyStorage.saveNBT());

        return ret; // Gibt die tatsächlich extrahierte Energiemenge zurück
    }

    // Gibt die aktuell gespeicherte Energiemenge zurück
    @Override
    public int getEnergyStored() {
        return energyStorage.getEnergyStored();
    }

    // Gibt die maximale Energiemenge zurück, die gespeichert werden kann
    @Override
    public int getMaxEnergyStored() {
        return energyStorage.getMaxEnergyStored();
    }

    // Gibt an, ob Energie extrahiert werden kann
    @Override
    public boolean canExtract() {
        return energyStorage.canExtract();
    }

    // Gibt an, ob Energie empfangen werden kann
    @Override
    public boolean canReceive() {
        return energyStorage.canReceive();
    }

    // Setzt die Energiemenge auf einen bestimmten Wert
    public void setEnergy(int energy) {
        energyStorage.setEnergy(energy);

        // Speichert den aktuellen Energiezustand im NBT-Tag des ItemStacks
        itemStack.getOrCreateTag().put("energy", energyStorage.saveNBT());
    }

    // Setzt die maximale Kapazität des Energiespeichers
    public void setCapacity(int capacity) {
        energyStorage.setCapacity(capacity);
    }
}
