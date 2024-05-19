package net.pasuki.power.energy;

import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;

/**
 * Eine Implementierung des IEnergizedPowerEnergyStorage, die nur Energie empfangen kann.
 */
public class ReceiveOnlyEnergyStorage implements IEnergizedPowerEnergyStorage {
    protected int energy; // Die aktuell gespeicherte Energiemenge
    protected int capacity; // Die maximale Energiemenge, die gespeichert werden kann
    protected int maxReceive; // Die maximale Energiemenge, die pro Tick empfangen werden kann

    /**
     * Standardkonstruktor.
     */
    public ReceiveOnlyEnergyStorage() {}

    /**
     * Konstruktor mit Anfangswerten.
     * @param energy Die initiale Energiemenge.
     * @param capacity Die maximale Kapazität.
     * @param maxReceive Die maximale Empfangsrate.
     */
    public ReceiveOnlyEnergyStorage(int energy, int capacity, int maxReceive) {
        this.energy = energy;
        this.capacity = capacity;
        this.maxReceive = maxReceive;
    }

    /**
     * Gibt die aktuelle Energiemenge zurück.
     * @return Die aktuelle Energiemenge.
     */
    @Override
    public int getEnergy() {
        return energy;
    }

    /**
     * Setzt die Energiemenge und aktualisiert den Zustand.
     * @param energy Die zu setzende Energiemenge.
     */
    @Override
    public void setEnergy(int energy) {
        this.energy = energy;
        onChange();
    }

    /**
     * Setzt die Energiemenge ohne den Zustand zu aktualisieren.
     * @param energy Die zu setzende Energiemenge.
     */
    @Override
    public void setEnergyWithoutUpdate(int energy) {
        this.energy = energy;
    }

    /**
     * Gibt die maximale Energiekapazität zurück.
     * @return Die maximale Energiekapazität.
     */
    @Override
    public int getCapacity() {
        return capacity;
    }

    /**
     * Setzt die Energiekapazität und aktualisiert den Zustand.
     * @param capacity Die zu setzende Kapazität.
     */
    @Override
    public void setCapacity(int capacity) {
        this.capacity = capacity;
        onChange();
    }

    /**
     * Setzt die Energiekapazität ohne den Zustand zu aktualisieren.
     * @param capacity Die zu setzende Kapazität.
     */
    @Override
    public void setCapacityWithoutUpdate(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Gibt die maximale Empfangsrate zurück.
     * @return Die maximale Empfangsrate.
     */
    public int getMaxReceive() {
        return maxReceive;
    }

    /**
     * Setzt die maximale Empfangsrate und aktualisiert den Zustand.
     * @param maxReceive Die zu setzende Empfangsrate.
     */
    public void setMaxReceive(int maxReceive) {
        this.maxReceive = maxReceive;
        onChange();
    }

    /**
     * Setzt die maximale Empfangsrate ohne den Zustand zu aktualisieren.
     * @param maxReceive Die zu setzende Empfangsrate.
     */
    public void setMaxReceiveWithoutUpdate(int maxReceive) {
        this.maxReceive = maxReceive;
    }

    /**
     * Wird aufgerufen, wenn sich der Zustand ändert.
     */
    protected void onChange() {}

    /**
     * Empfängt Energie bis zur maximalen Empfangsrate oder der maximalen Kapazität.
     * @param maxReceive Die maximale Energiemenge, die empfangen werden kann.
     * @param simulate Ob die Operation nur simuliert wird.
     * @return Die tatsächlich empfangene Energiemenge.
     */
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        int received = Math.min(getMaxEnergyStored() - energy, Math.min(getMaxReceive(), maxReceive));

        if (!simulate) {
            energy += received;
            onChange();
        }

        return received;
    }

    /**
     * Diese Implementierung erlaubt kein Extrahieren von Energie.
     * @param maxExtract Die maximale Energiemenge, die extrahiert werden kann.
     * @param simulate Ob die Operation nur simuliert wird.
     * @return Immer 0, da keine Energie extrahiert werden kann.
     */
    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    /**
     * Gibt die aktuell gespeicherte Energiemenge zurück.
     * @return Die aktuell gespeicherte Energiemenge.
     */
    @Override
    public final int getEnergyStored() {
        return getEnergy();
    }

    /**
     * Gibt die maximale Energiemenge zurück, die gespeichert werden kann.
     * @return Die maximale Energiemenge.
     */
    @Override
    public final int getMaxEnergyStored() {
        return getCapacity();
    }

    /**
     * Gibt an, ob Energie extrahiert werden kann.
     * @return Immer false, da kein Extrahieren erlaubt ist.
     */
    @Override
    public boolean canExtract() {
        return false;
    }

    /**
     * Gibt an, ob Energie empfangen werden kann.
     * @return Immer true, da Empfangen erlaubt ist.
     */
    @Override
    public boolean canReceive() {
        return true;
    }

    /**
     * Speichert den Energiezustand in einem NBT-Tag und gibt diesen zurück.
     * @return Das NBT-Tag mit dem gespeicherten Energiezustand.
     */
    @Override
    public Tag saveNBT() {
        return IntTag.valueOf(energy);
    }

    /**
     * Lädt den Energiezustand aus einem NBT-Tag.
     * @param tag Das NBT-Tag mit dem Energiezustand.
     */
    @Override
    public void loadNBT(Tag tag) {
        if (!(tag instanceof IntTag)) {
            energy = 0;
            return;
        }

        energy = ((IntTag) tag).getAsInt();
    }
}
