package net.pasuki.power.items;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.pasuki.power.tools.AdaptedEnergyStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class EnergizedToolItem extends Item {
    public static final String ENERGY_TAG = "Energy";

    public static final int MAXTRANSFER = 100;
    public static final int CAPACITY = 250;

    public EnergizedToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new EnergyCapabilityProvider(stack);
    }

    public static class EnergyCapabilityProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
        private final EnergyStorage energyStorage;
        private final LazyOptional<IEnergyStorage> energyHandler;

        public EnergyCapabilityProvider(ItemStack stack) {
            this.energyStorage = new EnergyStorage(CAPACITY, MAXTRANSFER) {
                @Override
                public int extractEnergy(int maxExtract, boolean simulate) {
                    return 0;
                }

                @Override
                public int receiveEnergy(int maxReceive, boolean simulate) {
                    int received = super.receiveEnergy(maxReceive, simulate);
                    if (!simulate) {
                        saveEnergyToStack(stack);
                    }
                    return received;
                }

                @Override
                public boolean canExtract() {
                    return false;
                }

                @Override
                public boolean canReceive() {
                    return true;
                }
            };
            this.energyHandler = LazyOptional.of(() -> new AdaptedEnergyStorage(energyStorage));
            if (stack.getTag() != null && stack.getTag().contains(ENERGY_TAG)) {
                energyStorage.deserializeNBT(stack.getTag().getCompound(ENERGY_TAG));
            }
        }

        private void saveEnergyToStack(ItemStack stack) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.put(ENERGY_TAG, energyStorage.serializeNBT());
        }

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == ForgeCapabilities.ENERGY) {
                return energyHandler.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put(ENERGY_TAG, energyStorage.serializeNBT());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            if (nbt.contains(ENERGY_TAG)) {
                energyStorage.deserializeNBT(nbt.getCompound(ENERGY_TAG));
            }
        }
    }
}
