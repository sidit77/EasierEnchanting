package easierenchanting;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.mixin.registry.sync.client.MixinMinecraftClient;
import net.fabricmc.fabric.mixin.resource.loader.MixinMinecraftGame;
import net.fabricmc.loader.FabricLoader;
import net.fabricmc.loader.launch.FabricClientTweaker;
import net.fabricmc.loader.launch.common.FabricMixinBootstrap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.screen.*;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Nameable;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

public class EasierEnchanting implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "easierenchanting";
    public static final String MOD_NAME = "Easier Enchanting";

    public static int lapiscost = 6;

    @Override
    public void onInitialize() {

        log(Level.INFO, "Initializing..");
        try {
            Path p = Paths.get("config/easierenchanting.txt");
            if(!Files.exists(p)){
                log(Level.INFO, "config not found");
                log(Level.INFO, "creating new config file");
                Files.write(Paths.get("config/easierenchanting.txt"), Collections.singletonList("lapiscost:6"));
            }
            for(String s : Files.readAllLines(p)){
                String[] tokens = s.split(":");
                switch(tokens[0].trim()){
                    case "lapiscost":
                        lapiscost = Math.max(0, Integer.parseInt(tokens[1].trim()));
                        log(Level.INFO, "setting lapis cost to " + lapiscost);
                        break;
                }
            }
        } catch (IOException e) {
            log(Level.ERROR, e.getMessage());
        }
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}