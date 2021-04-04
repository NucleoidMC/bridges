package xyz.nucleoid.bridges.game.map;

import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.bridges.game.BridgesConfig;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;

import java.io.IOException;

public class BridgesMapGenerator {

    private final BridgesConfig config;

    public BridgesMapGenerator(BridgesConfig config) {
        this.config = config;
    }

    public BridgesMap build() {
        MapTemplate template = MapTemplate.createEmpty();
        try{
            MapTemplateSerializer.INSTANCE.loadFromResource(config.map);
        } catch (IOException ignored) {

        }
        BridgesMap map = new BridgesMap(template, this.config);

        //this.buildSpawn(template);
        map.spawn = new BlockPos(0,65,0);

        return map;
    }

}
