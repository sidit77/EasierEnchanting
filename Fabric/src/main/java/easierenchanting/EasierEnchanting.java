package easierenchanting;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                if ("lapiscost".equals(tokens[0].trim())) {
                    lapiscost = Math.max(0, Integer.parseInt(tokens[1].trim()));
                    log(Level.INFO, "setting lapis cost to " + lapiscost);
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