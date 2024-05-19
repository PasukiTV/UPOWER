package net.pasuki.power.util;

import net.minecraft.util.Mth;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Locale;

public final class EnergyUtils {
    // Array von Präfixen für die Energiewerte (k = Kilo, M = Mega, etc.)
    private static final String[] ENERGY_PREFIXES = new String[] {
            "", "k", "M", "G", "T", "P"
    };

    // Privater Konstruktor, um die Instanziierung der Utility-Klasse zu verhindern
    private EnergyUtils() {}

    /**
     * Konvertiert einen Energiewert in eine Zeichenkette mit entsprechendem Präfix.
     * @param energy Der Energiewert in FE (Forge Energy).
     * @return Eine formatierte Zeichenkette mit dem Energiewert und dem Präfix.
     */
    public static String getEnergyWithPrefix(int energy) {
        // Wenn der Energiewert kleiner als 1000 ist, wird er ohne Präfix zurückgegeben
        if (energy < 1000) {
            return String.format(Locale.ENGLISH, "%d FE", energy);
        }

        // Kopie des Energiewertes zur Bearbeitung als double für Division
        double energyWithPrefix = energy;
        int prefixIndex = 0;

        // Solange der Energiewert größer oder gleich 1000 ist und es weitere Präfixe gibt
        while (energyWithPrefix >= 1000 && prefixIndex < ENERGY_PREFIXES.length - 1) {
            energyWithPrefix /= 1000;
            prefixIndex++;
        }

        // Formatierte Zeichenkette mit dem Energiewert und dem entsprechenden Präfix zurückgeben
        return String.format(Locale.ENGLISH, "%.2f%s FE", energyWithPrefix, ENERGY_PREFIXES[prefixIndex]);
    }

    /**
     * Berechnet die Redstone-Signalstärke basierend auf der gespeicherten Energie.
     * @param energyStorage Die Energie-Speicher-Instanz.
     * @return Die Redstone-Signalstärke von 0 bis 15.
     */
    public static int getRedstoneSignalFromEnergyStorage(IEnergyStorage energyStorage) {
        // Prüfen, ob der Energiespeicher leer ist
        int storedEnergy = energyStorage.getEnergyStored();
        int maxEnergy = energyStorage.getMaxEnergyStored();
        boolean isEmpty = storedEnergy == 0;

        // Berechnung der Redstone-Signalstärke:
        // (Aktuelle Energie / Maximale Energie) * 14, aufgerundet
        // Falls der Energiespeicher nicht leer ist, wird 1 addiert, um ein Minimumsignal zu gewährleisten
        int signal = Math.min(Mth.floor((float) storedEnergy / maxEnergy * 14.f) + (isEmpty ? 0 : 1), 15);

        return signal;
    }
}
