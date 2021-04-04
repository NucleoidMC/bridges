package xyz.nucleoid.bridges.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class BridgesConfig {
    public static final Codec<BridgesConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            Identifier.CODEC.fieldOf("map").forGetter(config -> config.map),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs)
    ).apply(instance, BridgesConfig::new));

    public final PlayerConfig playerConfig;
    public final Identifier map;
    public final int timeLimitSecs;

    public BridgesConfig(PlayerConfig players, Identifier map, int timeLimitSecs) {
        this.playerConfig = players;
        this.map = map;
        this.timeLimitSecs = timeLimitSecs;
    }
}
