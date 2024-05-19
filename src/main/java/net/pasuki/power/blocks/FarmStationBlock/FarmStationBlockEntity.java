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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"NullableProblems", "DataFlowIssue"})
public class FarmStationBlockEntity extends BlockEntity {

    public static final String ENERGY_TAG = "Energy";
    public static final String ITEMS_TAG = "Inventory";
    public static final int MAXRECEIVE = 1000;
    public static final int CAPACITY = 10000;
    public static final int ENERGY_CONSUMPTION_PLANT = 50;
    public static final int ENERGY_CONSUMPTION_HARVEST = 100;
    public static final int SLOT_COUNT = 1;
    public static final int SLOT = 0;

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

    public void tickServer() {
        boolean powered = energy.getEnergyStored() > 0;
        if (powered != getBlockState().getValue(BlockStateProperties.POWERED)) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.POWERED, powered));
        }

        if (powered) {
            performFarmingOperations();
        }
    }

    private void performFarmingOperations() {
        BlockPos pos = getBlockPos();
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        List<BlockPos> positions = get3x3FieldPositions(pos, facing);

        // Middle block of the 3x3 field
        BlockPos middlePos = positions.get(4); // 4th index in a 0-based list of 9 elements is the middle one
        if (convertToWater(middlePos)) {
            for (BlockPos targetPos : positions) {
                if (!targetPos.equals(middlePos)) {
                    BlockState state = level.getBlockState(targetPos);
                    if (state.getBlock() instanceof FarmBlock || state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.GRASS_BLOCK) {
                        handleFarmingOperations(targetPos);
                    }
                }
            }
        }
    }

    private boolean convertToWater(BlockPos pos) {
        if (level.getBlockState(pos).getBlock() != Blocks.WATER) {
            int energyToConvert = ENERGY_CONSUMPTION_PLANT;  // Use the same energy as planting
            if (energy.extractEnergy(energyToConvert, true) >= energyToConvert) {
                level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
                energy.extractEnergy(energyToConvert, false);
                return true;
            }
            return false;
        }
        return true;
    }

    private void handleFarmingOperations(BlockPos targetPos) {
        BlockPos abovePos = targetPos.above();
        BlockState aboveState = level.getBlockState(abovePos);

        BlockState targetState = level.getBlockState(targetPos);
        Block targetBlock = targetState.getBlock();

        if (targetBlock == Blocks.DIRT || targetBlock == Blocks.GRASS_BLOCK) {
            convertToFarmland(targetPos);
        }

        if (aboveState.isAir() && hasSeeds() && level.getBlockState(targetPos).getBlock() instanceof FarmBlock) {
            plantSeed(abovePos);
        } else if (aboveState.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(aboveState)) {
            harvestCrop(abovePos, cropBlock, aboveState);
        }
    }

    private void convertToFarmland(BlockPos pos) {
        if (level.getBlockState(pos).getBlock() != Blocks.FARMLAND) {
            int energyToConvert = ENERGY_CONSUMPTION_PLANT;  // Use the same energy as planting
            if (energy.extractEnergy(energyToConvert, true) >= energyToConvert) {
                level.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
                energy.extractEnergy(energyToConvert, false);
            }
        }
    }

    private void plantSeed(BlockPos abovePos) {
        Item seedItem = getSeedItem();
        Block cropBlock = getCropBlock(seedItem);
        int energyToPlant = ENERGY_CONSUMPTION_PLANT;
        if (energy.extractEnergy(energyToPlant, true) >= energyToPlant) {
            level.setBlockAndUpdate(abovePos, cropBlock.defaultBlockState());
            energy.extractEnergy(energyToPlant, false);
            consumeSeed();
        }
    }

    private void harvestCrop(BlockPos abovePos, CropBlock cropBlock, BlockState aboveState) {
        int energyToHarvest = ENERGY_CONSUMPTION_HARVEST;
        if (energy.extractEnergy(energyToHarvest, true) >= energyToHarvest) {
            List<ItemStack> drops = cropBlock.getDrops(aboveState, (ServerLevel)level, abovePos, null);
            addToStorage(drops);
            level.setBlockAndUpdate(abovePos, Blocks.AIR.defaultBlockState());
            energy.extractEnergy(energyToHarvest, false);
        }
    }

    private void addToStorage(List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            if (isPreferredItem(drop.getItem())) {
                addPreferredItemToInternalStorageOrChest(drop);
            } else {
                if (!addToNearbyChest(drop)) {
                    dropItemInWorld(drop);
                }
            }
        }
    }

    private boolean isPreferredItem(Item item) {
        return item == Items.CARROT || item == Items.POTATO || item == Items.WHEAT_SEEDS || item == Items.BEETROOT_SEEDS;
    }

    private void addPreferredItemToInternalStorageOrChest(ItemStack itemStack) {
        ItemStack remaining = ItemHandlerHelper.insertItem(items, itemStack, false);
        if (!remaining.isEmpty()) {
            if (!addToNearbyChest(remaining)) {
                dropItemInWorld(remaining);
            }
        }
    }

    private void dropItemInWorld(ItemStack itemStack) {
        BlockPos pos = getBlockPos();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.addFreshEntity(new net.minecraft.world.entity.item.ItemEntity(serverLevel, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, itemStack));
        }
    }

    private List<BlockPos> get3x3FieldPositions(BlockPos pos, Direction facing) {
        List<BlockPos> positions = new ArrayList<>();
        Direction opposite = facing.getOpposite();

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                positions.add(getOffsetPos(pos, opposite, x, z));
            }
        }
        return positions;
    }

    private BlockPos getOffsetPos(BlockPos pos, Direction opposite, int x, int z) {
        return switch (opposite) {
            case NORTH -> pos.offset(x, -1, z - 2);
            case SOUTH -> pos.offset(x, -1, z + 2);
            case WEST -> pos.offset(z - 2, -1, x);
            case EAST -> pos.offset(z + 2, -1, x);
            default -> pos;
        };
    }

    private boolean addToNearbyChest(ItemStack itemStack) {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        Direction[] directions = {facing.getCounterClockWise(), facing.getClockWise()};

        for (Direction direction : directions) {
            if (tryAddToChest(worldPosition.relative(direction), itemStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryAddToChest(BlockPos chestPos, ItemStack itemStack) {
        BlockEntity entity = level.getBlockEntity(chestPos);
        if (entity instanceof ChestBlockEntity chest) {
            LazyOptional<IItemHandler> chestHandler = chest.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
            if (chestHandler.isPresent()) {
                IItemHandler itemHandler = chestHandler.orElseThrow(IllegalStateException::new);
                ItemStack remaining = ItemHandlerHelper.insertItem(itemHandler, itemStack, false);
                return remaining.isEmpty();
            }
        }
        return false;
    }

    private boolean isChestNearby() {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        Direction[] directions = {facing.getCounterClockWise(), facing.getClockWise()};

        for (Direction direction : directions) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof ChestBlockEntity) {
                return true;
            }
        }
        return false;
    }

    private boolean isChestFull() {
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        Direction[] directions = {facing.getCounterClockWise(), facing.getClockWise()};

        for (Direction direction : directions) {
            if (isChestAtCapacity(worldPosition.relative(direction))) {
                return true;
            }
        }
        return false;
    }

    private boolean isChestAtCapacity(BlockPos chestPos) {
        BlockEntity entity = level.getBlockEntity(chestPos);
        if (entity instanceof ChestBlockEntity chest) {
            LazyOptional<IItemHandler> chestHandler = chest.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
            if (chestHandler.isPresent()) {
                IItemHandler itemHandler = chestHandler.orElseThrow(IllegalStateException::new);
                for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
                    if (stackInSlot.isEmpty() || stackInSlot.getCount() < stackInSlot.getMaxStackSize()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean hasSeeds() {
        return !items.getStackInSlot(SLOT).isEmpty();
    }

    private Item getSeedItem() {
        return items.getStackInSlot(SLOT).getItem();
    }

    private Block getCropBlock(Item seedItem) {
        String itemName = ForgeRegistries.ITEMS.getKey(seedItem).toString();
        return switch (itemName) {
            case "minecraft:wheat_seeds" -> Blocks.WHEAT;
            case "minecraft:carrot" -> Blocks.CARROTS;
            case "minecraft:potato" -> Blocks.POTATOES;
            case "minecraft:beetroot_seeds" -> Blocks.BEETROOTS;
            default -> Blocks.WHEAT;
        };
    }

    private void consumeSeed() {
        ItemStack stack = items.getStackInSlot(SLOT);
        stack.shrink(1);
        if (stack.isEmpty()) {
            items.setStackInSlot(SLOT, ItemStack.EMPTY);
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(ITEMS_TAG, items.serializeNBT());
        tag.put(ENERGY_TAG, energy.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(ITEMS_TAG)) items.deserializeNBT(tag.getCompound(ITEMS_TAG));
        if (tag.contains(ENERGY_TAG)) energy.deserializeNBT(tag.get(ENERGY_TAG));
    }

    @Nonnull
    private ItemStackHandler createItemHandler() {
        return new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
    }

    @Nonnull
    private EnergyStorage createEnergyStorage() {
        return new EnergyStorage(CAPACITY, MAXRECEIVE);
    }

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

    public int getStoredPower() {
        return energy.getEnergyStored();
    }
}
