package net.pasuki.power.datagen;

import net.pasuki.power.Registration;
import net.pasuki.power.Power;
import net.pasuki.power.blocks.GeneratorBlock;
import net.pasuki.power.cables.blocks.FacadeBlockItem;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class TutLanguageProvider extends LanguageProvider {

    public TutLanguageProvider(PackOutput output, String locale) {
        super(output, Power.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add(Registration.GENERATOR_BLOCK.get(), "Power Generator");
        add(Registration.CHARGER_BLOCK.get(), "Charger");
        add(Registration.CABLE_BLOCK.get(), "Cable");
        add(Registration.FACADE_BLOCK.get(), "Facade");
        add(GeneratorBlock.SCREEN_GENERATOR, "Generator");
        add(FacadeBlockItem.FACADE_IS_MIMICING, "Facade is mimicking %s");
        add("tab.tutpower", "Tutorial Power");
    }
}
