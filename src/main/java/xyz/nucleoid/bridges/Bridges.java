package xyz.nucleoid.bridges;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.bridges.game.BridgesActive;
import xyz.nucleoid.bridges.game.BridgesConfig;
import xyz.nucleoid.bridges.game.BridgesGoalBlock;
import xyz.nucleoid.bridges.game.BridgesWaiting;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.GameType;

import java.util.WeakHashMap;

public class Bridges implements ModInitializer {
    public static final String ID = "bridges";
    public static final Logger LOGGER = LogManager.getLogger(ID);
    public static final GameType<BridgesConfig> TYPE = GameType.register(
            new Identifier(Bridges.ID, "bridges"),
            BridgesConfig.CODEC,
            BridgesWaiting::open
    );

    public static WeakHashMap<GameSpace, BridgesActive> GAMES = new WeakHashMap<>();

    public static Block BRIDGES_GOAL_BLOCK;

    @Override
    public void onInitialize() {
        BRIDGES_GOAL_BLOCK = Registry.register(Registry.BLOCK, new Identifier(ID, "goal_block"), new BridgesGoalBlock(FabricBlockSettings.of(Material.PORTAL).noCollision().strength(-1)));
    }
}
