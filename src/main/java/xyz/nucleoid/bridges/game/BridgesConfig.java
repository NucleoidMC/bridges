package xyz.nucleoid.bridges.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeamList;

import java.util.List;

public record BridgesConfig(PlayerConfig playerConfig, GameTeamList teams, Identifier map, int timeLimitSecs, int pointWinThreshold, Identifier dimension, List<ItemStack> items) {
    public static final Codec<BridgesConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            GameTeamList.CODEC.fieldOf("teams").forGetter(config -> config.teams),
            Identifier.CODEC.fieldOf("map").forGetter(config -> config.map),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs),
            Codec.INT.fieldOf("point_win_threshold").forGetter(config -> config.pointWinThreshold),
            Identifier.CODEC.optionalFieldOf("dimension", new Identifier("minecraft", "overworld")).forGetter(config -> config.dimension),
            ItemStack.CODEC.listOf().fieldOf("items").forGetter(config -> config.items)
    ).apply(instance, BridgesConfig::new));
}
