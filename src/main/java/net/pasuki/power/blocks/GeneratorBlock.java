package net.pasuki.power.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"deprecation", "NullableProblems"})
public class GeneratorBlock extends Block implements EntityBlock {

    public static final String SCREEN_GENERATOR = "screen.generator";

    public GeneratorBlock() {
        super(BlockBehaviour.Properties.of()
                .strength(3.5F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new GeneratorBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        } else {
            return (lvl, pos, st, be) -> {
                if (be instanceof GeneratorBlockEntity generator) {
                    generator.tickServer();
                }
            };
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && player.getItemInHand(hand).getItem() == Items.IRON_SWORD) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GeneratorBlockEntity GeneratorBlockEntity) { // Ersetzen Sie "YourBlockEntity" durch den tatsächlichen Typ Ihrer Block-Entität
                Direction blockFacing = state.getValue(BlockStateProperties.FACING); // Angenommen, die Ausrichtung ist mit "FACING" definiert

                // Ermitteln Sie die Seite des Blocks, die angeklickt wurde
                Direction clickedSide = trace.getDirection();

                // Überprüfen Sie, ob die angeklickte Seite mit der Ausrichtung übereinstimmt
                if (clickedSide == Direction.UP) {
                    GeneratorBlockEntity.setOutputTop(!GeneratorBlockEntity.isOutputTop());
                    player.sendSystemMessage(Component.literal("OUTPUT TOP " + " toggled to " + GeneratorBlockEntity.OUTPUT_TOP));
                } // TOP
                if (clickedSide == Direction.DOWN) {
                    GeneratorBlockEntity.setOutputBottom(!GeneratorBlockEntity.isOutputBottom());
                    player.sendSystemMessage(Component.literal("OUTPUT BOTTOM " + " toggled to " + GeneratorBlockEntity.OUTPUT_BOTTOM));
                } // BOTTOM
                if (clickedSide == blockFacing.getCounterClockWise()) {
                    GeneratorBlockEntity.setOutputRight(!GeneratorBlockEntity.isOutputRight());
                    player.sendSystemMessage(Component.literal("OUTPUT RIGHT " + " toggled to " + GeneratorBlockEntity.OUTPUT_RIGHT));
                } // RIGHT
                if (clickedSide == blockFacing.getClockWise()) {
                    GeneratorBlockEntity.setOutputLeft(!GeneratorBlockEntity.isOutputLeft());
                    player.sendSystemMessage(Component.literal("OUTPUT LEFT " + " toggled to " + GeneratorBlockEntity.OUTPUT_LEFT));
                } // LEFT
                if (clickedSide == blockFacing) {
                    GeneratorBlockEntity.setOutputFront(!GeneratorBlockEntity.isOutputFront());
                    player.sendSystemMessage(Component.literal("OUTPUT FRONT " + " toggled to " + GeneratorBlockEntity.OUTPUT_FRONT));
                } // FRONT
                if (clickedSide == blockFacing.getOpposite()) {
                    GeneratorBlockEntity.setOutputRear(!GeneratorBlockEntity.isOutputRear());
                    player.sendSystemMessage(Component.literal("OUTPUT REAR " + " toggled to " + GeneratorBlockEntity.OUTPUT_REAR));
                } // REAR
            }
        }
        if (!level.isClientSide && hand == InteractionHand.MAIN_HAND && !(player.getItemInHand(hand).getItem() == Items.IRON_SWORD)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof GeneratorBlockEntity) {
                MenuProvider containerProvider = new MenuProvider() {
                    @Override
                    public Component getDisplayName() {
                        return Component.translatable(SCREEN_GENERATOR);
                    }

                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
                        return new GeneratorContainer(windowId, playerEntity, pos);
                    }
                };
                NetworkHooks.openScreen((ServerPlayer) player, containerProvider, be.getBlockPos());
            } else {
                throw new IllegalStateException("Our named container provider is missing!");
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(BlockStateProperties.FACING, context.getHorizontalDirection().getOpposite())
                .setValue(BlockStateProperties.POWERED, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.POWERED, BlockStateProperties.FACING);
    }
}
