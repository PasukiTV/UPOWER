package net.pasuki.power.datagen;

import net.pasuki.power.Registration;
import net.pasuki.power.Power;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class TutBlockTags extends BlockTagsProvider {

    public TutBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Power.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(Registration.GENERATOR_BLOCK.get(),
                Registration.CHARGER_BLOCK.get()).add(Registration.CABLE_BLOCK.get(),
                Registration.FACADE_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(Registration.GENERATOR_BLOCK.get(),
                Registration.CHARGER_BLOCK.get()).add(Registration.CABLE_BLOCK.get(),
                Registration.FACADE_BLOCK.get());
     }
}
