package net.pasuki.power.energy;

import net.minecraft.nbt.Tag;
import net.minecraftforge.energy.IEnergyStorage;

public interface IEnergizedPowerEnergyStorage extends IEnergyStorage {
    // Gibt die aktuelle Energiemenge zurück
    int getEnergy();

    // Setzt die Energiemenge und aktualisiert den Zustand
    void setEnergy(int energy);

    // Setzt die Energiemenge ohne den Zustand zu aktualisieren
    void setEnergyWithoutUpdate(int energy);

    // Gibt die maximale Energiekapazität zurück
    int getCapacity();

    // Setzt die Energiekapazität und aktualisiert den Zustand
    void setCapacity(int capacity);

    // Setzt die Energiekapazität ohne den Zustand zu aktualisieren
    void setCapacityWithoutUpdate(int capacity);

    // Speichert den Energiezustand in einem NBT-Tag und gibt diesen zurück
    Tag saveNBT();

    // Lädt den Energiezustand aus einem NBT-Tag
    void loadNBT(Tag tag);
}
