package net.pasuki.power;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.pasuki.power.blocks.ChargerBlock.ChargerBlock;
import net.pasuki.power.blocks.ChargerBlock.ChargerBlockEntity;
import net.pasuki.power.blocks.FarmStationBlock.FarmStationBlock;
import net.pasuki.power.blocks.FarmStationBlock.FarmStationBlockContainer;
import net.pasuki.power.blocks.FarmStationBlock.FarmStationBlockEntity;
import net.pasuki.power.blocks.GeneratorBlock.GeneratorBlock;
import net.pasuki.power.blocks.GeneratorBlock.GeneratorBlockContainer;
import net.pasuki.power.blocks.GeneratorBlock.GeneratorBlockEntity;
import net.pasuki.power.cables.blocks.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.pasuki.power.energy.IEnergizedPowerEnergyStorage;
import net.pasuki.power.items.energy.EnergizedPowerPickaxe;

@SuppressWarnings("DataFlowIssue")
public class Registration {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Power.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Power.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Power.MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Power.MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Power.MODID);



    public static final RegistryObject<GeneratorBlock> GENERATOR_BLOCK = BLOCKS.register("generator_block", GeneratorBlock::new);
    public static final RegistryObject<Item> GENERATOR_BLOCK_ITEM = ITEMS.register("generator_block", () -> new BlockItem(GENERATOR_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<GeneratorBlockEntity>> GENERATOR_BLOCK_ENTITY = BLOCK_ENTITIES.register("generator_block",
            () -> BlockEntityType.Builder.of(GeneratorBlockEntity::new, GENERATOR_BLOCK.get()).build(null));
    public static final RegistryObject<MenuType<GeneratorBlockContainer>> GENERATOR_BLOCK_CONTAINER = MENU_TYPES.register("generator_block",
            () -> IForgeMenuType.create((windowId, inv, data) -> new GeneratorBlockContainer(windowId, inv.player, data.readBlockPos())));

    public static final RegistryObject<ChargerBlock> CHARGER_BLOCK = BLOCKS.register("charger_block", ChargerBlock::new);
    public static final RegistryObject<Item> CHARGER_BLOCK_ITEM = ITEMS.register("charger_block", () -> new BlockItem(CHARGER_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<ChargerBlockEntity>> CHARGER_BLOCK_ENTITY = BLOCK_ENTITIES.register("charger_block",
            () -> BlockEntityType.Builder.of(ChargerBlockEntity::new, CHARGER_BLOCK.get()).build(null));

    public static final RegistryObject<FarmStationBlock> FARM_STATION_BLOCK = BLOCKS.register("farm_station_block", FarmStationBlock::new);
    public static final RegistryObject<Item> FARM_STATION_BLOCK_ITEM = ITEMS.register("farm_station_block", () -> new BlockItem(FARM_STATION_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<FarmStationBlockEntity>> FARM_STATION_BLOCK_ENTITY = BLOCK_ENTITIES.register("farm_station_block",
            () -> BlockEntityType.Builder.of(FarmStationBlockEntity::new, FARM_STATION_BLOCK.get()).build(null));
    public static final RegistryObject<MenuType<FarmStationBlockContainer>> FARM_STATION_BLOCK_CONTAINER = MENU_TYPES.register("farm_station_block",
            () -> IForgeMenuType.create((windowId, inv, data) -> new FarmStationBlockContainer(windowId, inv.player, data.readBlockPos())));

    public static final RegistryObject<CableBlock> CABLE_BLOCK = BLOCKS.register("cable", CableBlock::new);
    public static final RegistryObject<Item> CABLE_BLOCK_ITEM = ITEMS.register("cable", () -> new BlockItem(CABLE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<CableBlockEntity>> CABLE_BLOCK_ENTITY = BLOCK_ENTITIES.register("cable",
            () -> BlockEntityType.Builder.of(CableBlockEntity::new, CABLE_BLOCK.get()).build(null));

    public static final RegistryObject<FacadeBlock> FACADE_BLOCK = BLOCKS.register("facade", FacadeBlock::new);
    public static final RegistryObject<Item> FACADE_BLOCK_ITEM = ITEMS.register("facade", () -> new FacadeBlockItem(FACADE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockEntityType<FacadeBlockEntity>> FACADE_BLOCK_ENTITY = BLOCK_ENTITIES.register("facade",
            () -> BlockEntityType.Builder.of(FacadeBlockEntity::new, FACADE_BLOCK.get()).build(null));

    // Energized Power Pickaxe
    public static final RegistryObject<Item> ENERGY_PICKAXE = ITEMS.register("energy_pickaxe",
            () -> new EnergizedPowerPickaxe(Tiers.DIAMOND, 1, -2.8F, new Item.Properties().stacksTo(1).durability(1561),
                    () -> new IEnergizedPowerEnergyStorage() {
                        private int energy = 0;
                        private final int capacity = 1000;

                        @Override
                        public int getEnergy() {
                            return energy;
                        }

                        @Override
                        public void setEnergy(int energy) {
                            this.energy = Math.min(energy, capacity);
                        }

                        @Override
                        public void setEnergyWithoutUpdate(int energy) {
                            this.energy = energy;
                        }

                        @Override
                        public int getCapacity() {
                            return capacity;
                        }

                        @Override
                        public void setCapacity(int capacity) {
                            // Do nothing, capacity is fixed
                        }

                        @Override
                        public void setCapacityWithoutUpdate(int capacity) {
                            // Do nothing, capacity is fixed
                        }

                        @Override
                        public Tag saveNBT() {
                            CompoundTag tag = new CompoundTag();
                            tag.putInt("Energy", energy);
                            tag.putInt("Capacity", capacity);
                            return tag;
                        }

                        @Override
                        public void loadNBT(Tag tag) {
                            if (tag instanceof CompoundTag) {
                                CompoundTag compoundTag = (CompoundTag) tag;
                                energy = compoundTag.getInt("Energy");
                            }
                        }

                        @Override
                        public int receiveEnergy(int maxReceive, boolean simulate) {
                            if (!canReceive()) {
                                return 0;
                            }

                            int energyReceived = Math.min(capacity - energy, maxReceive);
                            if (!simulate) {
                                energy += energyReceived;
                            }
                            return energyReceived;
                        }

                        @Override
                        public int extractEnergy(int maxExtract, boolean simulate) {
                            if (!canExtract()) {
                                return 0;
                            }

                            int energyExtracted = Math.min(energy, maxExtract);
                            if (!simulate) {
                                energy -= energyExtracted;
                            }
                            return energyExtracted;
                        }

                        @Override
                        public int getEnergyStored() {
                            return energy;
                        }

                        @Override
                        public int getMaxEnergyStored() {
                            return capacity;
                        }

                        @Override
                        public boolean canExtract() {
                            return true;
                        }

                        @Override
                        public boolean canReceive() {
                            return true;
                        }
                    }));

    public static RegistryObject<CreativeModeTab> TAB = TABS.register("tutpower", () -> CreativeModeTab.builder()
            .title(Component.translatable("tab.tutpower"))
            .icon(() -> new ItemStack(GENERATOR_BLOCK.get()))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((featureFlags, output) -> {
                output.accept(GENERATOR_BLOCK.get());
                output.accept(CHARGER_BLOCK.get());
                output.accept(CABLE_BLOCK.get());
                output.accept(FACADE_BLOCK.get());
                output.accept(FARM_STATION_BLOCK.get());
                output.accept(ENERGY_PICKAXE.get());
            })
            .build());

    public static void init(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        TABS.register(modEventBus);
    }

}
