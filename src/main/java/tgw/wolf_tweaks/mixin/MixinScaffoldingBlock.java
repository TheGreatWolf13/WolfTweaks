package tgw.wolf_tweaks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.wolf_tweaks.WolfTweaks;

@Mixin(ScaffoldingBlock.class)
public abstract class MixinScaffoldingBlock extends Block implements SimpleWaterloggedBlock {

    @Shadow @Final public static IntegerProperty DISTANCE;

    public MixinScaffoldingBlock(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "onPlace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"))
    private void onPlace_scheduleTick(Level level, BlockPos pos, Block block, int tickrate) {
        if (!WolfTweaks.preventScaffoldingDoubleTicking) {
            level.scheduleTick(pos, block, tickrate);
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private boolean tick_destroyBlock(ServerLevel level, BlockPos pos, boolean drop) {
        WolfTweaks.isDroppingScaffolding = true;
        WolfTweaks.findScaffoldingDroppingPos(level, pos);
        boolean ret = level.destroyBlock(pos, drop);
        WolfTweaks.removeDependencies(level, pos);
        WolfTweaks.isDroppingScaffolding = false;
        return ret;
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean tick_setBlock(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        WolfTweaks.preventScaffoldingDoubleTicking = true;
        boolean ret = level.setBlock(pos, state, flags);
        WolfTweaks.preventScaffoldingDoubleTicking = false;
        return ret;
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Inject(method = "tick", at = @At("TAIL"))
    private void tick_tail(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci, @Local(ordinal = 1) BlockState newState) {
        if (state == newState && state.getValue(DISTANCE) != 7) {
            WolfTweaks.removeDependencies(level, pos);
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Inject(method = "updateShape", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ScheduledTickAccess;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"))
    private void updateShape_scheduleTick(BlockState state, LevelReader level, ScheduledTickAccess scheduler, BlockPos pos, Direction fromDir, BlockPos fromPos, BlockState fromState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
        WolfTweaks.addScaffoldingDependency((Level) level, pos.asLong(), fromPos.asLong());
    }
}
