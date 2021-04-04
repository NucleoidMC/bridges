package xyz.nucleoid.bridges;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.bridges.game.BridgesConfig;
import xyz.nucleoid.bridges.game.BridgesWaiting;
import xyz.nucleoid.plasmid.game.GameType;

public class Bridges implements ModInitializer {
    public static final String ID = "bridges";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final GameType<BridgesConfig> TYPE = GameType.register(
            new Identifier(Bridges.ID, "bridges"),
            BridgesWaiting::open,
            BridgesConfig.CODEC
    );
    @Override
    public void onInitialize() {
    }
}
