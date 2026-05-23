package nrz.bettercam;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ModConfig {
    public boolean enableFirstPersonZoom = true;
    public boolean enableTransitions = true;
    public boolean enableCtrlLock = true;
    public boolean hasAcceptedMultiplayerWarning = false;
    public float hudSwayStrength = 0.3f;

    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "better-camera.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    public static ModConfig INSTANCE = new ModConfig();

    public static void load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
                if (INSTANCE == null) {
                    INSTANCE = new ModConfig();
                }
            } catch (Exception e) {
                BetterCamera.LOGGER.error("Failed to load config, using defaults", e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            BetterCamera.LOGGER.error("Failed to save config", e);
        }
    }
}
