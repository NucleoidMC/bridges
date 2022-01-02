package xyz.nucleoid.bridges.game.map;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import xyz.nucleoid.bridges.Bridges;
import xyz.nucleoid.bridges.game.BridgesActive;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.plasmid.game.common.team.GameTeam;
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

    public TeamRegions getRegions(GameTeamKey team) {
        return teamRegions.get(team);
    }

    public ChunkGenerator asGenerator(MinecraftServer server) {
        return new TemplateChunkGenerator(server, this.template);
    }

    public void updateTeams(GameTeamList teams, BridgesActive active) {
        teams.forEach(team -> {
            var id = team.key().id();
            var spawn = template.getMetadata().getFirstRegionBounds(id + "_spawn");
            var goal = template.getMetadata().getFirstRegionBounds(id + "_goal");
            var base = template.getMetadata().getFirstRegionBounds(id + "_base");
            this.teamRegions.put(team.key(), new TeamRegions(spawn, goal, base));
            if (goal == null) {
                throw new IllegalStateException("No goal provided for team " + team.key().id());
            }
            goal.forEach(pos -> {
                // Set the goal to be composed of goal blocks
                active.getWorld().setBlockState(pos, Bridges.BRIDGES_GOAL_BLOCK.getDefaultState());
            });
        });
    }

    public boolean allowBlockInteractions(BlockPos pos) {
        return teamRegions.values().stream().noneMatch(regions -> regions.base.contains(pos));
    }

    public record TeamRegions(BlockBounds spawn, BlockBounds goal, BlockBounds base) {
    }
}
