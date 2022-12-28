package xyz.nucleoid.bridges.game;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.bridges.Bridges;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

@SuppressWarnings("deprecation")
public class BridgesGoalBlock extends Block implements PolymerBlock {

    public BridgesGoalBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.END_PORTAL;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.END_PORTAL.getDefaultState();
    }

    @Override
    public boolean canSynchronizeToPolymerClient(ServerPlayerEntity player) {
        return false;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (GameSpaceManager.get().hasGame(world) && entity instanceof ServerPlayerEntity player) {
            var active = Bridges.GAMES.get(GameSpaceManager.get().byWorld(world));
            active.tickPlayerInGoal(player);
        }
        super.onEntityCollision(state, world, pos, entity);
    }
}
