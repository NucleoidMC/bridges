package xyz.nucleoid.bridges.game;

import net.minecraft.server.network.ServerPlayerEntity;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;

public record BridgesPlayer(ServerPlayerEntity player, GameTeam team) {
    // TODO data about the player in the game
}
