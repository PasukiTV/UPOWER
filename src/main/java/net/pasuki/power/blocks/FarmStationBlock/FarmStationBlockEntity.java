package net.pasuki.power.blocks.FarmStationBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.pasuki.power.Registration;
import net.pasuki.power.tools.AdaptedEnergyStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"NullableProblems", "DataFlowIssue"})
public class FarmStationBlockEntity extends BlockEntity {

    // Konstante Definitionen
    public static final String ENERGY_TAG = "Energy";
    public static final String ITEMS_TAG = "Inventory";
    public static final int MAXRECEIVE = 1000;
    public static final int CAPACITY = 10000;
    public static final int ENERGY_CONSUMPTION_PLANT = 50;
    public static final int ENERGY_CONSUMPTION_HARVEST = 100;
    public static final int SLOT_COUNT = 1;
    public static final int SLOT = 0;

    // Inventar- und Energielager
    private final ItemStackHandler items = createItemHandler();
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private final EnergyStorage energy = createEnergyStorage();
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> new AdaptedEnergyStorage(energy) {
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            setChanged();
            return super.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    });

    public FarmStationBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.FARM_STATION_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * Serverseitige Tick-Methode, die die Energie überprüft und Farming-Operationen durchführt.
     */
    public void tickServer() {
        boolean powered = energy.getEnergyStored() > 0;
        if (powered != getBlockState().getValue(BlockStateProperties.POWERED)) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.POWERED, powered));
        }

        if (powered) {
            performFarmingOperations();
        }
    }

    /**
     * Führt die Farming-Operationen wie Pflanzen und Ernten auf einem 3x3-Feld an der Rückseite des Blocks aus.
     */
    private void performFarmingOperations() {
        logEnergy("Start of Farming Operations", energy.getEnergyStored());
        BlockPos pos = getBlockPos();
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        List<BlockPos> positions = get3x3FieldPositions(pos, facing);

        for (BlockPos targetPos : positions) {
            BlockState state = level.getBlockState(targetPos);
            if (state.getBlock() instanceof FarmBlock) {
                BlockPos abovePos = targetPos.above();
                BlockState aboveState = level.getBlockState(abovePos);

                // Pflanzen-Logik
                if (aboveState.isAir() && hasSeeds()) {
                    Item seedItem = getSeedItem();
                    Block cropBlock = getCropBlock(seedItem);
                    int energyToPlant = ENERGY_CONSUMPTION_PLANT;
                    if (energy.extractEnergy(energyToPlant, true) >= energyToPlant) {
                        logEnergy("Before Planting", energy.getEnergyStored());
                        level.setBlockAndUpdate(abovePos, cropBlock.defaultBlockState());
                        energy.extractEnergy(energyToPlant, false);
                        consumeSeed();
                        logEnergy("After Planting", energy.getEnergyStored());
                    }
                }

                // Ernte-Logik
                else if (aboveState.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(aboveState)) {
                    int energyToHarvest = ENERGY_CONSUMPTION_HARVEST;
                    if (energy.extractEnergy(energyToHarvest, true) >= energyToHarvest) {
                        logEnergy("Before Harvesting", energy.getEnergyStored());

                        boolean chestNearby = isChestNearby();
                        boolean chestFull = isChestFull();

                        if (chestNearby && chestFull) {
                            logEnergy("Chest is full, skipping harvest", energy.getEnergyStored());
                            continue;
                        }

                        List<ItemStack> drops = cropBlock.getDrops(aboveState, (ServerLevel)level, abovePos, null);
                        ItemStack seeds = new ItemStack(getSeedItem(), 1);

                        boolean cropAdded = false;
                        if (chestNearby) {
                            for (ItemStack drop : drops) {
                                cropAdded = addToNearbyChest(drop, facing);
                                if (!cropAdded) {
                                    cropBlock.popResource(level, abovePos, drop);
                                }
                            }
                        } else {
                            for (ItemStack drop : drops) {
                                cropBlock.popResource(level, abovePos, drop);
                            }
                        }

                        boolean seedsAdded = addItemToInventory(seeds);
                        if (!seedsAdded) {
                            cropBlock.popResource(level, abovePos, seeds);
                        }

                        if (cropAdded || !seeds.isEmpty()) {
                            level.setBlockAndUpdate(abovePos, Blocks.AIR.defaultBlockState());
                        }

                        energy.extractEnergy(energyToHarvest, false);
                        logEnergy("After Harvesting", energy.getEnergyStored());
                    }
                }
            }
        }

        logEnergy("End of Farming Operations", energy.getEnergyStored());
    }

    /**
     * Berechnet die Positionen des 3x3-Feldes an der Rückseite des Blocks basierend auf der Blickrichtung.
     *
     * @param pos die Position des Blocks.
     * @param facing die Blickrichtung des Blocks.
     * @return eine Liste von BlockPositionen, die das 3x3-Feld darstellen.
     */
    private List<BlockPos> get3x3FieldPositions(BlockPos pos, Direction facing) {
        List<BlockPos> positions = new ArrayList<>();
        Direction opposite = facing.getOpposite();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockPos offsetPos = pos;
                switch (opposite) {
                    case NORTH:
                        offsetPos = pos.offset(x, -1, z - 2); // Feld hinter dem Block nach Norden
                        break;
                    case SOUTH:
                        offsetPos = pos.offset(x, -1, z + 2); // Feld hinter dem Block nach Süden
                        break;
                    case WEST:
                        offsetPos = pos.offset(z - 2, -1, x); // Feld hinter dem Block nach Westen
                        break;
                    case EAST:
                        offsetPos = pos.offset(z + 2, -1, x); // Feld hinter dem Block nach Osten
                        break;
                    default:
                        break;
                }
                positions.add(offsetPos);
            }
        }
        return positions;
    }

    /**
     * Protokolliert den aktuellen Energiezustand für Debug-Zwecke.
     *
     * @param message die Nachricht, die protokolliert werden soll.
     * @param currentEnergy der aktuelle Energiezustand.
     */
    private void logEnergy(String message, int currentEnergy) {
        System.out.println(message + ": " + currentEnergy);
    }

    /**
     * Fügt ein Item dem Inventar hinzu.
     *
     * @param itemStack das hinzuzufügende ItemStack.
     * @return true, wenn das Item vollständig hinzugefügt wurde, false, wenn ein Teil übrig blieb.
     */
    private boolean addItemToInventory(ItemStack itemStack) {
        ItemStack remaining = ItemHandlerHelper.insertItem(items, itemStack, false);
        return remaining.isEmpty();
    }

    /**
     * Fügt ein Item einer nahegelegenen Truhe hinzu, basierend auf dem facing des Blocks.
     *
     * @param itemStack das hinzuzufügende ItemStack.
     * @param facing die Blickrichtung des Blocks.
     * @return true, wenn das Item vollständig hinzugefügt wurde, false, wenn ein Teil übrig blieb.
     */
    private boolean addToNearbyChest(ItemStack itemStack, Direction facing) {
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        for (Direction direction : new Direction[]{left, right}) {
            BlockPos chestPos = worldPosition.relative(direction);
            BlockEntity entity = level.getBlockEntity(chestPos);
            if (entity instanceof ChestBlockEntity chest) {
                LazyOptional<IItemHandler> chestHandler = chest.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite());
                if (chestHandler.isPresent()) {
                    IItemHandler itemHandler = chestHandler.orElseThrow(IllegalStateException::new);
                    ItemStack remaining = ItemHandlerHelper.insertItem(itemHandler, itemStack, false);
                    if (remaining.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Überprüft, ob eine nahegelegene Truhe vorhanden ist.
     *
     * @return true, wenn eine Truhe vorhanden ist, false andernfalls.
     */
    private boolean isChestNearby() {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        for (Direction direction : new Direction[]{left, right}) {
            BlockPos chestPos = worldPosition.relative(direction);
            BlockEntity entity = level.getBlockEntity(chestPos);
            if (entity instanceof ChestBlockEntity) {
                return true;
            }
        }
        return false;
    }

    /**
     * Überprüft, ob die angeschlossene Truhe voll ist.
     *
     * @return true, wenn die Truhe voll ist, false andernfalls.
     */
    private boolean isChestFull() {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        for (Direction direction : new Direction[]{left, right}) {
            BlockPos chestPos = worldPosition.relative(direction);
            BlockEntity entity = level.getBlockEntity(chestPos);
            if (entity instanceof ChestBlockEntity chest) {
                LazyOptional<IItemHandler> chestHandler = chest.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite());
                if (chestHandler.isPresent()) {
                    IItemHandler itemHandler = chestHandler.orElseThrow(IllegalStateException::new);
                    for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                        ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
                        if (stackInSlot.isEmpty() || (stackInSlot.getItem() == Items.WHEAT && stackInSlot.getCount() < stackInSlot.getMaxStackSize())) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Überprüft, ob der Slot Samen enthält.
     *
     * @return true, wenn Samen vorhanden sind, false andernfalls.
     */
    private boolean hasSeeds() {
        ItemStack seedStack = items.getStackInSlot(SLOT);
        return !seedStack.isEmpty();
    }

    /**
     * Gibt das zu pflanzende Item basierend auf dem aktuellen Seed zurück.
     *
     * @return das zu pflanzende Item.
     */
    private Item getSeedItem() {
        ItemStack seedStack = items.getStackInSlot(SLOT);
        return seedStack.getItem();
    }

    /**
     * Gibt den zu pflanzenden Crop-Block basierend auf dem Seed-Item zurück.
     *
     * @param seedItem das Seed-Item.
     * @return der zu pflanzende Crop-Block.
     */
    private Block getCropBlock(Item seedItem) {
        if (seedItem == Items.WHEAT_SEEDS) {
            return Blocks.WHEAT;
        } else if (seedItem == Items.CARROT) {
            return Blocks.CARROTS;
        } else if (seedItem == Items.POTATO) {
            return Blocks.POTATOES;
        } else if (seedItem == Items.BEETROOT_SEEDS) {
            return Blocks.BEETROOTS;
        } else {
            return Blocks.WHEAT; // Default fallback
        }
    }

    /**
     * Konsumiert einen Samen aus dem Inventar.
     */
    private void consumeSeed() {
        ItemStack stack = items.getStackInSlot(SLOT);
        if (!stack.isEmpty()) {
            stack.shrink(1);
        }
        if (stack.isEmpty()) {
            items.setStackInSlot(SLOT, ItemStack.EMPTY);
        }
    }

    /**
     * Gibt das ItemStackHandler-Objekt zurück.
     *
     * @return das ItemStackHandler-Objekt.
     */
    public ItemStackHandler getItems() {
        return items;
    }

    /**
     * Speichert zusätzliche Daten in den NBT-Tag.
     *
     * @param tag der NBT-Tag.
     */
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(ITEMS_TAG, items.serializeNBT());
        tag.put(ENERGY_TAG, energy.serializeNBT());
    }

    /**
     * Lädt Daten aus dem NBT-Tag.
     *
     * @param tag der NBT-Tag.
     */
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(ITEMS_TAG)) items.deserializeNBT(tag.getCompound(ITEMS_TAG));
        if (tag.contains(ENERGY_TAG)) energy.deserializeNBT(tag.get(ENERGY_TAG));
    }

    /**
     * Erstellt den ItemStackHandler für das Inventar.
     *
     * @return ein neuer ItemStackHandler.
     */
    @Nonnull
    private ItemStackHandler createItemHandler() {
        return new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    /**
     * Erstellt die EnergyStorage für das BlockEntity.
     *
     * @return eine neue EnergyStorage-Instanz.
     */
    @Nonnull
    private EnergyStorage createEnergyStorage() {
        return new EnergyStorage(CAPACITY, MAXRECEIVE);
    }

    /**
     * Gibt die angeforderte Capability zurück.
     *
     * @param cap die Capability.
     * @param side die Seite, von der die Capability angefragt wird.
     * @param <T> der Typ der Capability.
     * @return die Capability oder LazyOptional.empty(), wenn nicht verfügbar.
     */
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        } else if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    /**
     * Gibt die gespeicherte Energie zurück.
     *
     * @return die gespeicherte Energie.
     */
    public int getStoredPower() {
        return energy.getEnergyStored();
    }
}
