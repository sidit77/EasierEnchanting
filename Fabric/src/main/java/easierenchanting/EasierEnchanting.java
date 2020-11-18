package easierenchanting;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class EasierEnchanting implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "easierenchanting";
    public static final String MOD_NAME = "Easier Enchanting";

    public static int lapiscost = 6;
    public static boolean hardmode = false;
    public static boolean cleantooltips = false;

    @Override
    public void onInitialize() {

        log(Level.INFO, "Initializing..");
        try {
            Path p = Paths.get("config/"+MOD_ID+".txt");
            if(!Files.exists(p)){
                log(Level.INFO, "config not found");
                Files.write(p.toAbsolutePath(), Arrays.asList(
                        "lapiscost:"+lapiscost,
                        "hardmode:"+hardmode,
                        "cleantooltips:"+cleantooltips
                ));
                log(Level.INFO, "created new config file");
            }
            for(String s : Files.readAllLines(p)){
                String[] tokens = s.split(":");
                switch(tokens[0].trim()){
                    case "lapiscost":
                        lapiscost = Math.max(0, Integer.parseInt(tokens[1].trim()));
                        log(Level.INFO, "setting lapis cost to " + lapiscost);
                        break;
                    case "hardmode":
                        hardmode = tokens[1].trim().contains("true");
                        if (hardmode)
                            log(Level.INFO, "running in hard mode");
                        break;
                    case "cleantooltips":
                        cleantooltips = tokens[1].trim().contains("true");
                        if (cleantooltips)
                            log(Level.INFO, "running with clean tooltips");
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