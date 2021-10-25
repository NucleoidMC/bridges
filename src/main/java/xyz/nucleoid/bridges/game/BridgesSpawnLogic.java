package xyz.nucleoid.bridges.game;

import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import xyz.nucleoid.bridges.Bridges;
import xyz.nucleoid.bridges.game.map.BridgesMap;
import xyz.nucleoid.plasmid.game.GameSpace;

public class BridgesSpawnLogic {
    private final GameSpace gameSpace;
    private final BridgesMap map;
    private final ServerWorld world;

    public BridgesSpawnLogic(GameSpace gameSpace, BridgesMap map, ServerWorld world) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.world = world;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.changeGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));
    }

    public void spawnPlayer(BridgesPlayer player) {
        BlockPos pos = this.map.center;
        if (pos == null) {
            Bridges.LOGGER.error("Cannot spawn player! No spawn is defined in the map!");
            return;
        }

        var spawn = map.getRegions(player.team()).spawn();
        var positions = BlockPos.stream(spawn.asBox()).toList();
        var spawnPos = positions.get(player.player().getRandom().nextInt(positions.size()));

        player.player().teleport(world, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 0.0F, 0.0F);
        player.player().lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, Vec3d.ofCenter(pos));
    }

    public void spawnPlayerAtCenter(ServerPlayerEntity player) {
        BlockPos pos = this.map.center;
        if (pos == null) {
            Bridges.LOGGER.error("Cannot spawn player! No spawn is defined in the map!");
            return;
        }
        player.teleport(world, pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
        player.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, Vec3d.ofCenter(pos));
    }
}
