package xyz.nucleoid.bridges.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.bridges.game.BridgesConfig;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;

import java.io.IOException;

public class BridgesMapGenerator {

    private final BridgesConfig config;

    public BridgesMapGenerator(BridgesConfig config) {
        this.config = config;
    }

    public BridgesMap build(MinecraftServer server) {
        MapTemplate template = MapTemplate.createEmpty();
        try{
            template = MapTemplateSerializer.loadFromResource(server, config.map());
        } catch (IOException ignored) {

        }

        return new BridgesMap(template);
    }

}
