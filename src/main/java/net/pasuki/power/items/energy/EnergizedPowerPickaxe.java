package net.pasuki.power.items.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.pasuki.power.energy.IEnergizedPowerEnergyStorage;
import net.pasuki.power.util.EnergyUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;

public class EnergizedPowerPickaxe extends PickaxeItem {
    private final Supplier<IEnergizedPowerEnergyStorage> energyStorageProvider;
    private static final int ENERGY_PER_TICK = 10; // Amount of energy to charge per tick
    private static final int ENERGY_PER_BLOCK = 250; // Energy cost per block mined

    public EnergizedPowerPickaxe(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties props, Supplier<IEnergizedPowerEnergyStorage> energyStorageProvider) {
        super(tier, attackDamageModifier, attackSpeedModifier, props);
        this.energyStorageProvider = energyStorageProvider;
    }

    protected static int getEnergy(ItemStack itemStack) {
        return itemStack.getCapability(ForgeCapabilities.ENERGY)
                .map(IEnergyStorage::getEnergyStored)
                .orElse(0);
    }

    protected static void setEnergy(ItemStack itemStack, int energy) {
        itemStack.getCapability(ForgeCapabilities.ENERGY)
                .ifPresent(cap -> ((ItemCapabilityEnergy) cap).setEnergy(energy));
    }

    protected static int getCapacity(ItemStack itemStack) {
        return itemStack.getCapability(ForgeCapabilities.ENERGY)
                .map(IEnergyStorage::getMaxEnergyStored)
                .orElse(0);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int capacity = getCapacity(stack);
        return capacity == 0 ? 0 : Math.round(getEnergy(stack) * 13.f / capacity);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float energyFraction = getCapacity(stack) == 0 ? 0.f : (float) getEnergy(stack) / getCapacity(stack);
        return Mth.hsvToRgb(energyFraction * .33f, 1.f, 1.f);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        components.add(Component.translatable("tooltip.energizedpower.energy_meter.content.txt",
                        EnergyUtils.getEnergyWithPrefix(getEnergy(itemStack)), EnergyUtils.getEnergyWithPrefix(getCapacity(itemStack)))
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ItemCapabilityEnergy(stack, stack.getTag(), energyStorageProvider.get());
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        ItemStack stack = player.getMainHandItem();
        return getEnergy(stack) >= ENERGY_PER_BLOCK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            IEnergyStorage energyStorage = stack.getCapability(ForgeCapabilities.ENERGY).orElse(null);
            if (energyStorage != null && energyStorage.canReceive()) {
                int energyToAdd = ENERGY_PER_TICK; // The amount of energy to add per tick
                int energyAdded = energyStorage.receiveEnergy(energyToAdd, false);
                player.displayClientMessage(Component.translatable("item.energizedpower.pickaxe.charging", energyAdded), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5F, 1.0F);
            }
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (getEnergy(stack) < ENERGY_PER_BLOCK) {
            return false;
        }

        if (!level.isClientSide) {
            if (entityLiving instanceof Player) {
                Player player = (Player) entityLiving;
                level.destroyBlock(pos, false, player); // Ensures the block is properly destroyed
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                int currentEnergy = getEnergy(stack);
                int newEnergy = currentEnergy - ENERGY_PER_BLOCK;
                setEnergy(stack, newEnergy);
                System.out.println("Energy before mining: " + currentEnergy);
                System.out.println("Energy after mining: " + newEnergy);
                player.displayClientMessage(Component.translatable("item.energizedpower.pickaxe.energy_used", ENERGY_PER_BLOCK), true);
            }
        }

        return super.mineBlock(stack, level, state, pos, entityLiving);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return getEnergy(stack) >= ENERGY_PER_BLOCK && super.isCorrectToolForDrops(stack, state);
    }
}
