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

    // Konstruktor der Klasse
    public EnergizedPowerEnergyItem(Properties props, Supplier<IEnergizedPowerEnergyStorage> energyStorageProvider) {
        super(props);
        this.energyStorageProvider = energyStorageProvider;
    }

    // Methode zum Abrufen der gespeicherten Energie eines ItemStacks
    protected static int getEnergy(ItemStack itemStack) {
        return itemStack.getCapability(ForgeCapabilities.ENERGY).orElse(null).getEnergyStored();
    }

    // Methode zum Setzen der gespeicherten Energie eines ItemStacks
    protected static void setEnergy(ItemStack itemStack, int energy) {
        ((ItemCapabilityEnergy)itemStack.getCapability(ForgeCapabilities.ENERGY).orElse(null)).setEnergy(energy);
    }

    // Methode zum Abrufen der maximalen Energiekapazität eines ItemStacks
    protected static int getCapacity(ItemStack itemStack) {
        return itemStack.getCapability(ForgeCapabilities.ENERGY).orElse(null).getMaxEnergyStored();
    }

    // Zeigt die Energieleiste an, wenn das Item im Inventar ist
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    // Berechnet die Breite der Energieleiste basierend auf der gespeicherten Energie und Kapazität
    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(getEnergy(stack) * 13.f / getCapacity(stack));
    }

    // Berechnet die Farbe der Energieleiste basierend auf dem Verhältnis der gespeicherten Energie zur Kapazität
    @Override
    public int getBarColor(ItemStack stack) {
        float f = Math.max(0.f, getEnergy(stack) / (float)getCapacity(stack));
        return Mth.hsvToRgb(f * .33f, 1.f, 1.f); // HSV zu RGB-Konvertierung
    }

    // Fügt dem Tooltip des Items den Energiewert und die Kapazität hinzu
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        components.add(Component.translatable("tooltip.energizedpower.energy_meter.content.txt",
                        EnergyUtils.getEnergyWithPrefix(getEnergy(itemStack)),
                        EnergyUtils.getEnergyWithPrefix(getCapacity(itemStack))).
                withStyle(ChatFormatting.GRAY));
    }

    // Initialisiert die Fähigkeiten des Items, einschließlich des Energie-Speichers
    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ItemCapabilityEnergy(stack, stack.getTag(), energyStorageProvider.get());
    }
}
