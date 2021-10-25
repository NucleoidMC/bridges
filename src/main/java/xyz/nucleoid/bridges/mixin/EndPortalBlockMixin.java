package xyz.nucleoid.bridges.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.EndPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.nucleoid.plasmid.game.manager.GameSpaceManager;

@Mixin(EndPortalBlock.class)
public class EndPortalBlockMixin {
    /*
    To prevent players being teleported to end, we cancel end portal logic if there is a game open in the world
     */

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    public void cancelEndPortalsInGameSpace(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (GameSpaceManager.get().hasGame(world)) {
            ci.cancel();
        }
    }
}
