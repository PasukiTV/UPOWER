package net.pasuki.power.energy;

import net.minecraft.nbt.Tag;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Interface für einen energiebasierten Energiespeicher, der zusätzliche
 * Methoden für das Speichern und Laden des Energiezustands sowie die
 * direkte Verwaltung von Energie und Kapazität bietet.
 */
public interface IEnergizedPowerEnergyStorage extends IEnergyStorage {

    /**
     * Gibt die aktuelle Energiemenge zurück.
     * @return Die aktuelle Energiemenge.
     */
    int getEnergy();

    /**
     * Setzt die Energiemenge und aktualisiert den Zustand.
     * @param energy Die zu setzende Energiemenge.
     */
    void setEnergy(int energy);

    /**
     * Setzt die Energiemenge ohne den Zustand zu aktualisieren.
     * @param energy Die zu setzende Energiemenge.
     */
    void setEnergyWithoutUpdate(int energy);

    /**
     * Gibt die maximale Energiekapazität zurück.
     * @return Die maximale Energiekapazität.
     */
    int getCapacity();

    /**
     * Setzt die Energiekapazität und aktualisiert den Zustand.
     * @param capacity Die zu setzende Kapazität.
     */
    void setCapacity(int capacity);

    /**
     * Setzt die Energiekapazität ohne den Zustand zu aktualisieren.
     * @param capacity Die zu setzende Kapazität.
     */
    void setCapacityWithoutUpdate(int capacity);

    /**
     * Speichert den Energiezustand in einem NBT-Tag und gibt diesen zurück.
     * @return Das NBT-Tag mit dem gespeicherten Energiezustand.
     */
    Tag saveNBT();

    /**
     * Lädt den Energiezustand aus einem NBT-Tag.
     * @param tag Das NBT-Tag mit dem Energiezustand.
     */
    void loadNBT(Tag tag);
}
