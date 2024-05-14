package net.pasuki.power.datagen;

import net.pasuki.power.Registration;
import net.pasuki.power.Power;
import net.pasuki.power.blocks.Charger.ChargerBlockEntity;
import net.pasuki.power.blocks.Generator.GeneratorBlockEntity;
import net.pasuki.power.cables.blocks.CableBlockEntity;
import net.pasuki.power.cables.blocks.FacadeBlockEntity;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.stream.Collectors;

public class ModLootTables extends VanillaBlockLoot {

    @Override
    protected void generate() {
        createStandardTable(Registration.GENERATOR_BLOCK.get(), Registration.GENERATOR_BLOCK_ENTITY.get(), GeneratorBlockEntity.ITEMS_TAG, GeneratorBlockEntity.ENERGY_TAG);
        createStandardTable(Registration.CHARGER_BLOCK.get(), Registration.CHARGER_BLOCK_ENTITY.get(), ChargerBlockEntity.ENERGY_TAG);
        createStandardTable(Registration.CABLE_BLOCK.get(), Registration.CABLE_BLOCK_ENTITY.get(), CableBlockEntity.ENERGY_TAG);
        createStandardTable(Registration.FACADE_BLOCK.get(), Registration.FACADE_BLOCK_ENTITY.get(), FacadeBlockEntity.MIMIC_TAG);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ForgeRegistries.BLOCKS.getEntries().stream()
                .filter(e -> e.getKey().location().getNamespace().equals(Power.MODID))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private void createStandardTable(Block block, BlockEntityType<?> type, String... tags) {
        LootPoolSingletonContainer.Builder<?> lti = LootItem.lootTableItem(block);
        lti.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY));
        for (String tag : tags) {
            lti.apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy(tag, "BlockEntityTag." + tag, CopyNbtFunction.MergeStrategy.REPLACE));
        }
        lti.apply(SetContainerContents.setContents(type).withEntry(DynamicLoot.dynamicEntry(new ResourceLocation("minecraft", "contents"))));

        LootPool.Builder builder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(lti);
        add(block, LootTable.lootTable().withPool(builder));
    }

}
