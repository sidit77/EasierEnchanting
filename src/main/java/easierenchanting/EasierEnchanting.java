package easierenchanting;

import net.fabricmc.api.ModInitializer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EasierEnchanting implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "easierenchanting";
    public static final String MOD_NAME = "Easier Enchanting";

    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing");
        //TODO: Initializer
    }

    public static void log(Level level, String message){
        LOGGER.log(level, "["+MOD_NAME+"] " + message);
    }

}