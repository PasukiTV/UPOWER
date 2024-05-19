package net.pasuki.power.items.energy;

import net.pasuki.power.energy.IEnergizedPowerEnergyStorage;
import net.pasuki.power.util.EnergyUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class EnergizedPowerEnergyItem extends Item {
    private final Supplier<IEnergizedPowerEnergyStorage> energyStorageProvider; // Lieferant für den Energie-Speicher

    /**
     * Konstruktor der Klasse.
     * @param props Die Eigenschaften des Items.
     * @param energyStorageProvider Der Lieferant für den Energie-Speicher.
     */
    public EnergizedPowerEnergyItem(Properties props, Supplier<IEnergizedPowerEnergyStorage> energyStorageProvider) {
        super(props);
        this.energyStorageProvider = energyStorageProvider;
    }

    /**
     * Methode zum Abrufen des Energie-Speicherobjekts eines ItemStacks.
     * @param itemStack Der ItemStack.
     * @return Das IEnergizedPowerEnergyStorage-Objekt.
     */
    private static IEnergizedPowerEnergyStorage getEnergyStorage(ItemStack itemStack) {
        return itemStack.getCapability(ForgeCapabilities.ENERGY)
                .filter(cap -> cap instanceof IEnergizedPowerEnergyStorage)
                .map(cap -> (IEnergizedPowerEnergyStorage) cap)
                .orElseThrow(() -> new IllegalArgumentException("Energy capability not present"));
    }

    /**
     * Zeigt die Energieleiste an, wenn das Item im Inventar ist.
     * @param stack Der ItemStack.
     * @return true, wenn die Energieleiste sichtbar ist.
     */
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    /**
     * Berechnet die Breite der Energieleiste basierend auf der gespeicherten Energie und Kapazität.
     * @param stack Der ItemStack.
     * @return Die Breite der Energieleiste.
     */
    @Override
    public int getBarWidth(ItemStack stack) {
        IEnergizedPowerEnergyStorage energyStorage = getEnergyStorage(stack);
        return Math.round(energyStorage.getEnergyStored() * 13.f / energyStorage.getMaxEnergyStored());
    }

    /**
     * Berechnet die Farbe der Energieleiste basierend auf dem Verhältnis der gespeicherten Energie zur Kapazität.
     * @param stack Der ItemStack.
     * @return Die Farbe der Energieleiste.
     */
    @Override
    public int getBarColor(ItemStack stack) {
        IEnergizedPowerEnergyStorage energyStorage = getEnergyStorage(stack);
        float f = Math.max(0.f, (float) energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored());
        return Mth.hsvToRgb(f * .33f, 1.f, 1.f); // Konvertiert HSV-Wert in RGB-Wert
    }

    /**
     * Fügt dem Tooltip des Items den Energiewert und die Kapazität hinzu.
     * @param itemStack Der ItemStack.
     * @param level Das Level.
     * @param components Die Liste der Komponenten.
     * @param tooltipFlag Das Tooltip-Flag.
     */
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        IEnergizedPowerEnergyStorage energyStorage = getEnergyStorage(itemStack);
        components.add(Component.translatable("tooltip.energizedpower.energy_meter.content.txt",
                        EnergyUtils.getEnergyWithPrefix(energyStorage.getEnergyStored()),
                        EnergyUtils.getEnergyWithPrefix(energyStorage.getMaxEnergyStored()))
                .withStyle(ChatFormatting.GRAY));
    }

    /**
     * Initialisiert die Fähigkeiten des Items, einschließlich des Energie-Speichers.
     * @param stack Der ItemStack.
     * @param nbt Das NBT-Tag.
     * @return Der ICapabilityProvider.
     */
    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ItemCapabilityEnergy(stack, stack.getTag(), energyStorageProvider.get());
    }
}

