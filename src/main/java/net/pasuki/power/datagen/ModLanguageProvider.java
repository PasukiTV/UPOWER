package net.pasuki.power.datagen;

import net.pasuki.power.Registration;
import net.pasuki.power.Power;
import net.pasuki.power.blocks.GeneratorBlock.GeneratorBlock;
import net.pasuki.power.cables.blocks.FacadeBlockItem;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    private final String locale;

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, Power.MODID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        if (locale.equals("en_us")) {
            addEnglishTranslations();
        } else if (locale.equals("de_de")) {
            addGermanTranslations();
        }
        // Weitere Sprachen hier hinzufügen
    }

    private void addEnglishTranslations() {
        add(Registration.GENERATOR_BLOCK.get(), "Power Generator");
        add(Registration.CHARGER_BLOCK.get(), "Charger");
        add(Registration.FARM_STATION_BLOCK.get(),"Farmstation");

        add(Registration.CABLE_BLOCK.get(), "Cable");
        add(Registration.FACADE_BLOCK.get(), "Facade");
        add(GeneratorBlock.SCREEN_GENERATOR, "Generator");
        add(FacadeBlockItem.FACADE_IS_MIMICING, "Facade is mimicking %s");
        add("tab.power", "Tutorial Power");

        // Item translations

        add(Registration.ENERGIZED_PICKAXE.get(), "Energized Pickaxe");
    }

    private void addGermanTranslations() {
        add(Registration.GENERATOR_BLOCK.get(), "Stromgenerator");
        add(Registration.CHARGER_BLOCK.get(), "Ladegerät");
        add(Registration.FARM_STATION_BLOCK.get(),"Farmstation");

        add(Registration.CABLE_BLOCK.get(), "Kabel");
        add(Registration.FACADE_BLOCK.get(), "Fassade");
        add(GeneratorBlock.SCREEN_GENERATOR, "Generator");
        add(FacadeBlockItem.FACADE_IS_MIMICING, "Die Fassade imitiert %s");
        add("tab.power", "Tutorial Strom");

        // Item translations

        add(Registration.ENERGIZED_PICKAXE.get(), "Energie-Spitzhacke");
    }
}
