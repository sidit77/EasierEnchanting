package com.sidit77.easierenchanting;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

@Mod("easierenchanting")
public class EasierEnchanting
{
    private static final Logger LOGGER = LogManager.getLogger();

    public static int lapiscost = 6;

    public EasierEnchanting() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        try {
            Path p = Paths.get("config/easierenchanting.txt");
            if(!Files.exists(p)){
                LOGGER.info("config not found");
                LOGGER.info("creating new config file");
                Files.write(Paths.get("config/easierenchanting.txt"), Collections.singletonList("lapiscost:6"));
            }
            for(String s : Files.readAllLines(p)){
                String[] tokens = s.split(":");
                switch(tokens[0].trim()){
                    case "lapiscost":
                        lapiscost = Math.max(0, Integer.parseInt(tokens[1].trim()));
                        LOGGER.info("setting lapis cost to " + lapiscost);
                        break;
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }


}
