package net.pasuki.power.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.pasuki.power.Registration;
import net.pasuki.power.tools.AdaptedEnergyStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings({"NullableProblems", "DataFlowIssue"})
public class GeneratorBlockEntity extends BlockEntity {

    public static final String ITEMS_TAG = "Inventory";
    public static final String ENERGY_TAG = "Energy";
    public static final int GENERATE = 50;
    public static final int MAXRECEIVE = 1000;
    public static final int MAXEXTRACT = 10;
    public static final int CAPACITY = 100000;

    public static final int SLOT_COUNT = 1;
    public static final int SLOT = 0;

    public Boolean OUTPUT_TOP = false;
    public Boolean OUTPUT_BOTTOM = false;
    public Boolean OUTPUT_FRONT = false;
    public Boolean OUTPUT_REAR = false;
    public Boolean OUTPUT_LEFT = false;
    public Boolean OUTPUT_RIGHT = false;

    private final ItemStackHandler items = createItemHandler();
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);

    public final EnergyStorage energy = createEnergyStorage();
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> new AdaptedEnergyStorage(energy) {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            setChanged();
            return super.receiveEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            setChanged();
            return super.extractEnergy(maxExtract, simulate);
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

    private int burnTime;

    public GeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.GENERATOR_BLOCK_ENTITY.get(), pos, state);
    }

    public void tickServer() {
        generateEnergy();
        distributeEnergy();
    }

    // Check if we have a burnable item in the inventory and if so generate energy
    private void generateEnergy() {
        if (energy.getEnergyStored() < energy.getMaxEnergyStored()) {
            if (burnTime <= 0) {
                ItemStack fuel = items.getStackInSlot(SLOT);
                if (fuel.isEmpty()) {
                    // No fuel
                    return;
                }
                setBurnTime(ForgeHooks.getBurnTime(fuel, RecipeType.SMELTING));
                if (burnTime <= 0) {
                    // Not a fuel
                    return;
                }
                items.extractItem(SLOT, 1, false);
            } else {
                setBurnTime(burnTime-1);
                energy.receiveEnergy(GENERATE, false);
            }
            setChanged();
        }
    }

    private void setBurnTime(int bt) {
        if (bt == burnTime) {
            return;
        }
        burnTime = bt;
        if (getBlockState().getValue(BlockStateProperties.POWERED) != burnTime > 0) {
            level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(BlockStateProperties.POWERED, burnTime > 0));
        }
        setChanged();
    }

    private void distributeEnergy() {
        if (energy.getEnergyStored() <= 0) {
            return;
        }

        // Holen Sie den aktuellen BlockState und die Ausrichtung
        BlockState state = level.getBlockState(getBlockPos());
        Direction facing = state.getValue(BlockStateProperties.FACING);  // Ersetzen Sie BlockStateProperties.HORIZONTAL_FACING durch die tatsächliche Property, wenn anders

        Direction TOP = Direction.UP;
        Direction BOTTOM = Direction.DOWN;

        Direction FRONT = facing;  // "vorne"
        Direction REAR = facing.getOpposite();      // "hinten"

        Direction LEFT = facing.getClockWise();  // Im Uhrzeigersinn für "links"
        Direction RIGHT = facing.getCounterClockWise();      // Gegen den Uhrzeigersinn für "rechts"

        // Durchlaufe nur die gewünschten Richtungen
        if (OUTPUT_TOP) {
            for (Direction direction : new Direction[]{TOP}) {
                BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
                if (be != null) {
                    be.getCapability(ForgeCapabilities.ENERGY).ifPresent(e -> {
                        if (e.canReceive()) {
                            int received = e.receiveEnergy(Math.min(energy.getEnergyStored(), MAXEXTRACT), false);
                            energy.extractEnergy(received, false);
                            setChanged();
                        }
                    });
                }
            }
        }
        if (OUTPUT_BOTTOM) {
            for (Direction direction : new Direction[]{BOTTOM}) {
                BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
                if (be != null) {
                    be.getCapability(ForgeCapabilities.ENERGY).ifPresent(e -> {
                        if (e.canReceive()) {
                            int received = e.receiveEnergy(Math.min(energy.getEnergyStored(), MAXEXTRACT), false);
                            energy.extractEnergy(received, false);
                            setChanged();
                        }
                    });
                }
            }
        }
        if (OUTPUT_REAR) {
            for (Direction direction : new Direction[]{REAR}) {
                BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
                if (be != null) {
                    be.getCapability(ForgeCapabilities.ENERGY).ifPresent(e -> {
                        if (e.canReceive()) {
                            int received = e.receiveEnergy(Math.min(energy.getEnergyStored(), MAXEXTRACT), false);
                            energy.extractEnergy(received, false);
                            setChanged();
                        }
                    });
                }
            }
        }
        if (OUTPUT_LEFT) {
            for (Direction direction : new Direction[]{LEFT}) {
                BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
                if (be != null) {
                    be.getCapability(ForgeCapabilities.ENERGY).ifPresent(e -> {
                        if (e.canReceive()) {
                            int received = e.receiveEnergy(Math.min(energy.getEnergyStored(), MAXEXTRACT), false);
                            energy.extractEnergy(received, false);
                            setChanged();
                        }
                    });
                }
            }
        }
        if (OUTPUT_RIGHT) {
            for (Direction direction : new Direction[]{RIGHT}) {
                BlockEntity be = level.getBlockEntity(getBlockPos().relative(direction));
                if (be != null) {
                    be.getCapability(ForgeCapabilities.ENERGY).ifPresent(e -> {
                        if (e.canReceive()) {
                            int received = e.receiveEnergy(Math.min(energy.getEnergyStored(), MAXEXTRACT), false);
                            energy.extractEnergy(received, false);
                            setChanged();
                        }
                    });
                }
            }
        }
    }


    public ItemStackHandler getItems() {
        return items;
    }

    public int getStoredPower() {
        return energy.getEnergyStored();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(ITEMS_TAG, items.serializeNBT());
        tag.put(ENERGY_TAG, energy.serializeNBT());
        tag.putInt("burnTime", burnTime);
        tag.putBoolean("output_top", OUTPUT_TOP);
        tag.putBoolean("output_bottom", OUTPUT_BOTTOM);
        tag.putBoolean("output_front", OUTPUT_FRONT);
        tag.putBoolean("output_rear", OUTPUT_REAR);
        tag.putBoolean("output_left", OUTPUT_LEFT);
        tag.putBoolean("output_right", OUTPUT_RIGHT);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(ITEMS_TAG)) items.deserializeNBT(tag.getCompound(ITEMS_TAG));
        if (tag.contains(ENERGY_TAG)) energy.deserializeNBT(tag.get(ENERGY_TAG));
        if (tag.contains("burnTime")) burnTime = tag.getInt("burnTime");
        if (tag.contains("output_top")) OUTPUT_TOP = tag.getBoolean("output_top");
        if (tag.contains("output_bottom")) OUTPUT_BOTTOM = tag.getBoolean("output_bottom");
        if (tag.contains("output_front")) OUTPUT_FRONT = tag.getBoolean("output_front");
        if (tag.contains("output_rear")) OUTPUT_REAR = tag.getBoolean("output_rear");
        if (tag.contains("output_left")) OUTPUT_LEFT = tag.getBoolean("output_left");
        if (tag.contains("output_right")) OUTPUT_RIGHT = tag.getBoolean("output_right");
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
        return new EnergyStorage(CAPACITY, MAXRECEIVE, MAXEXTRACT);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        } else if (cap == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        } else {
            return super.getCapability(cap, side);
        }
    }




    public void setOutputTop(boolean b) {
        OUTPUT_TOP = b;
        setChanged();
    }

    public boolean isOutputTop() {
        return OUTPUT_TOP;
    }

    public void setOutputBottom(boolean b) {
        OUTPUT_BOTTOM = b;
        setChanged();
    }

    public boolean isOutputBottom() {
        return OUTPUT_BOTTOM;
    }

    public void setOutputFront(boolean b) {
        OUTPUT_FRONT = b;
        setChanged();
    }

    public boolean isOutputFront() {
        return OUTPUT_FRONT;
    }

    public void setOutputRear(boolean b) {
        OUTPUT_REAR = b;
        setChanged();
    }

    public boolean isOutputRear() {
        return OUTPUT_REAR;
    }

    public void setOutputLeft(boolean b) {
        OUTPUT_LEFT = b;
        setChanged();
    }

    public boolean isOutputLeft() {
        return OUTPUT_LEFT;
    }

    public void setOutputRight(boolean b) {
        OUTPUT_RIGHT = b;
        setChanged();
    }

    public boolean isOutputRight() {
        return OUTPUT_RIGHT;
    }

}
