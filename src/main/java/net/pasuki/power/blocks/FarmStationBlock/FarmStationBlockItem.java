package net.pasuki.power.blocks.FarmStationBlock;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.pasuki.power.util.EnergyUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FarmStationBlockItem extends BlockItem {

    public FarmStationBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> components, TooltipFlag tooltipFlag) {
        components.add(Component.translatable("tooltip.farmstation.energy_meter.content.txt",
                        EnergyUtils.getEnergyWithPrefix(getEnergy(itemStack)), EnergyUtils.getEnergyWithPrefix(getCapacity(itemStack)))
                .withStyle(ChatFormatting.GRAY));
    }

    // Method to get the energy of the item stack
    private int getEnergy(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag != null && tag.contains(FarmStationBlockEntity.ENERGY_TAG)) {
            return tag.getInt(FarmStationBlockEntity.ENERGY_TAG);
        }
        return 0;
    }

    // Method to get the capacity of the item stack
    private int getCapacity(ItemStack itemStack) {
        return FarmStationBlockEntity.CAPACITY;
    }
}
