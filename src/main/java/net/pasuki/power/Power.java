package net.pasuki.power;

import net.pasuki.power.compat.TopCompatibility;
import net.pasuki.power.datagen.DataGeneration;
import net.pasuki.power.network.Channel;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Power.MODID)
public class Power {

    public static final String MODID = "power";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Power() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        Registration.init(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(DataGeneration::generate);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        Channel.register();
        TopCompatibility.register();
    }
}
