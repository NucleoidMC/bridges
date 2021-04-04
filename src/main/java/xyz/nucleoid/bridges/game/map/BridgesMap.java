package xyz.nucleoid.bridges.game.map;

import net.minecraft.server.MinecraftServer;
import xyz.nucleoid.bridges.game.BridgesConfig;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public class BridgesMap {
    private final MapTemplate template;
    private final BridgesConfig config;
    public BlockPos spawn;

    public BridgesMap(MapTemplate template, BridgesConfig config) {
        this.template = template;
        this.config = config;
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }
}
