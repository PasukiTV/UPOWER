package net.pasuki.power.items;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.pasuki.power.energy.ReceiveOnlyEnergyStorage;
import net.pasuki.power.items.energy.ItemCapabilityEnergy;
import net.pasuki.power.util.EnergyUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;

public class EnergizedPickaxe extends PickaxeItem {
    private static final int CAPACITY = 1000; // Maximale Energiemenge
    private static final int MAXRECEIVE = 1000; // Maximale Energiemenge die empfangen werden kann
    private static final int ENERGY_PER_TICK = 10; // Energiemenge pro Tick zum Aufladen
    private static final int ENERGY_PER_BLOCK = 250; // Energiekosten pro abgebautem Block
    private static final float DIAMOND_SPEED = 8.0F; // Geschwindigkeit bei genug Energie
    private static final float HAND_SPEED = 0.25F; // Geschwindigkeit bei zu wenig Energie
    private final Supplier<ReceiveOnlyEnergyStorage> energyStorageProvider; // Lieferant für den Energie-Speicher

    // Konstruktor der Klasse
    public EnergizedPickaxe() {
        super(Tiers.DIAMOND, 1, -2.8F, new Item.Properties().stacksTo(1));
        this.energyStorageProvider = () -> new ReceiveOnlyEnergyStorage(0, CAPACITY, MAXRECEIVE);
    }

    // Methode zum Abrufen der gespeicherten Energie eines ItemStacks
    private static int getEnergy(ItemStack itemStack) {
        return itemStack.getCapability(ForgeCapabilities.ENERGY)
                .map(IEnergyStorage::getEnergyStored)
                .orElse(0);
    }

    // Methode zum Setzen der gespeicherten Energie eines ItemStacks
    private static void setEnergy(ItemStack itemStack, int energy) {
        itemStack.getCapability(ForgeCapabilities.ENERGY)
                .ifPresent(cap -> ((ItemCapabilityEnergy) cap).setEnergy(energy));
    }

    // Methode zum Abrufen der maximalen Energiekapazität eines ItemStacks
    private static int getCapacity(ItemStack itemStack) {
        return itemStack.getCapability(ForgeCapabilities.ENERGY)
                .map(IEnergyStorage::getMaxEnergyStored)
                .orElse(0);
    }

    // Zeigt die Energieleiste an, wenn das Item im Inventar ist
    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    // Berechnet die Breite der Energieleiste basierend auf der gespeicherten Energie und Kapazität
    @Override
    public int getBarWidth(ItemStack stack) {
        int capacity = getCapacity(stack);
        return capacity == 0 ? 0 : Math.round(getEnergy(stack) * 13.f / capacity);
    }

    // Berechnet die Farbe der Energieleiste basierend auf dem Verhältnis der gespeicherten Energie zur Kapazität
    @Override
    public int getBarColor(ItemStack stack) {
        float energyFraction = getCapacity(stack) == 0 ? 0.f : (float) getEnergy(stack) / getCapacity(stack);
        return Mth.hsvToRgb(energyFraction * .33f, 1.f, 1.f);
    }

    // Fügt dem Tooltip des Items den Energiewert und die Kapazität hinzu
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        components.add(Component.translatable("tooltip.energizedpower.energy_meter.content.txt",
                        EnergyUtils.getEnergyWithPrefix(getEnergy(itemStack)), EnergyUtils.getEnergyWithPrefix(getCapacity(itemStack)))
                .withStyle(ChatFormatting.GRAY));
    }

    // Initialisiert die Fähigkeiten des Items, einschließlich des Energie-Speichers
    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ItemCapabilityEnergy(stack, stack.getTag(), energyStorageProvider.get());
    }

    // Überprüft, ob ein Block abgebaut werden kann, basierend auf der verfügbaren Energie
    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return true; // Blöcke können jederzeit abgebaut werden
    }

    // Handhabung der Interaktion beim Benutzen des Items
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            IEnergyStorage energyStorage = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            if (energyStorage != null && energyStorage.canReceive()) {
                int energyAdded = energyStorage.receiveEnergy(ENERGY_PER_TICK, false);
                player.displayClientMessage(Component.translatable("item.energizedpower.pickaxe.charging", energyAdded), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }
        return InteractionResultHolder.success(stack);
    }

    // Handhabung des Blockabbaus, inklusive Energiekosten und Sound
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (entityLiving instanceof Player) {
            Player player = (Player) entityLiving;
            int currentEnergy = getEnergy(stack);
            if (currentEnergy >= ENERGY_PER_BLOCK) {
                if (!level.isClientSide) {
                    level.destroyBlock(pos, false, player);
                    level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                    setEnergy(stack, currentEnergy - ENERGY_PER_BLOCK);
                    player.displayClientMessage(Component.translatable("item.energizedpower.pickaxe.energy_used", ENERGY_PER_BLOCK), true);
                }
            } else {
                level.playSound(null, pos, SoundEvents.ANVIL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        return super.mineBlock(stack, level, state, pos, entityLiving);
    }

    // Überprüft, ob das Item das richtige Werkzeug für den Abbau eines Blocks ist
    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return getEnergy(stack) >= ENERGY_PER_BLOCK || super.isCorrectToolForDrops(stack, state);
    }

    // Bestimmt die Abbaugeschwindigkeit des Items
    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return getEnergy(stack) >= ENERGY_PER_BLOCK ? DIAMOND_SPEED : HAND_SPEED;
    }

    // Verhindert den Haltbarkeitsverlust des Items
    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public int getDamage(ItemStack stack) {
        return 0;
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        // Keine Aktion, da das Item keine Haltbarkeit hat
    }
}

