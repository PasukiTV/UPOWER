package net.pasuki.power.items;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.common.util.LazyOptional;

public class EnergizedPickaxeItem extends PickaxeItem {

    public EnergizedPickaxeItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Item.Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entityLiving) {
        if (entityLiving instanceof Player) {
            if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
                LazyOptional<IEnergyStorage> energyCap = stack.getCapability(ForgeCapabilities.ENERGY);
                if (energyCap.isPresent()) {
                    IEnergyStorage energyStorage = energyCap.orElseThrow(IllegalStateException::new);
                    if (energyStorage.extractEnergy(50, true) >= 50) {
                        energyStorage.extractEnergy(50, false);
                        return super.mineBlock(stack, level, state, pos, entityLiving);
                    } else {
                        return false; // Verhindere das Minen, wenn keine Energie vorhanden ist
                    }
                }
            }
        }
        return super.mineBlock(stack, level, state, pos, entityLiving);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player) {
            LazyOptional<IEnergyStorage> energyCap = stack.getCapability(ForgeCapabilities.ENERGY);
            if (energyCap.isPresent()) {
                IEnergyStorage energyStorage = energyCap.orElseThrow(IllegalStateException::new);
                if (energyStorage.extractEnergy(100, true) >= 100) {
                    energyStorage.extractEnergy(100, false);
                    return super.hurtEnemy(stack, target, attacker);
                }
            }
        }
        return false;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }
}
