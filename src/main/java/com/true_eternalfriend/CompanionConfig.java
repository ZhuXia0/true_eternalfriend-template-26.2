package com.true_eternalfriend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

/** A single optional server setting; absent or invalid values use the documented default. */
public final class CompanionConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("true_eternal_friend.properties");
    private static int eatCdTicks = CompanionLogic.EAT_CD_TICKS;
    private CompanionConfig() { }

    public static void load() {
        if (!Files.isRegularFile(FILE)) return;
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(FILE)) {
            properties.load(reader);
            eatCdTicks = Math.max(1, Integer.parseInt(properties.getProperty("eatCdTicks", "1200")));
        } catch (IOException | NumberFormatException exception) {
            eatCdTicks = CompanionLogic.EAT_CD_TICKS;
        }
    }

    public static int eatCdTicks() { return eatCdTicks; }
}
