package xyz.nucleoid.bridges.game.map;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.bridges.game.BridgesConfig;
import xyz.nucleoid.bridges.game.BridgesPlayer;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
import xyz.nucleoid.plasmid.game.common.team.GameTeamConfig;
import xyz.nucleoid.plasmid.game.common.team.GameTeamKey;
import xyz.nucleoid.plasmid.game.common.team.GameTeamList;
import xyz.nucleoid.plasmid.game.world.generator.TemplateChunkGenerator;

import java.util.Map;

public class BridgesMap {
    private final MapTemplate template;
    private final Map<GameTeamKey, TeamRegions> teamRegions = new Reference2ObjectOpenHashMap<>();
    public Vec3d center;

    public BridgesMap(MapTemplate template) {
        this.template = template;
        var bounds = template.getMetadata().getFirstRegionBounds("center");
        this.center = bounds == null ? new Vec3d(0, 80, 0) : bounds.center();
    }

    public TeamRegions getRegions(GameTeam team) {
        return teamRegions.get(team.key());
    }

    public boolean isInGoal(BridgesPlayer bridgesPlayer) {
        var player = bridgesPlayer.player();
        return this.teamRegions.values().stream().anyMatch(teamRegions -> {
            if (getRegions(bridgesPlayer.team()).goal.asBox().contains(player.getPos())) return false;
            return teamRegions.goal.asBox().contains(player.getPos());
        });

    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }

    public void updateTeams(GameTeamList teams) {
        teams.forEach(team -> {
            var id = team.key().id();
            var spawn = template.getMetadata().getFirstRegionBounds(id + "_spawn");
            var goal = template.getMetadata().getFirstRegionBounds(id + "_goal");
            var base = template.getMetadata().getFirstRegionBounds(id + "_base");
            this.teamRegions.put(team.key(), new TeamRegions(spawn, goal, base));
        });
    }

    public boolean allowBlockInteractions(BlockPos pos) {
        return teamRegions.values().stream().noneMatch(regions -> regions.base.contains(pos));
    }

    public record TeamRegions(BlockBounds spawn, BlockBounds goal, BlockBounds base) {
    }
}
