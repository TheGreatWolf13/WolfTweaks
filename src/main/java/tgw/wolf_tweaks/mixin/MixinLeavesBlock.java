package tgw.wolf_tweaks.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LeavesBlock.class)
public abstract class MixinLeavesBlock extends Block implements SimpleWaterloggedBlock {

    @Shadow @Final public static IntegerProperty DISTANCE;
    @Shadow @Final public static BooleanProperty PERSISTENT;

    public MixinLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Shadow
    protected abstract void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource);

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean tick_setBlock(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        if (!state.getValue(PERSISTENT) && state.getValue(DISTANCE) == 7) {
            RandomSource random = level.getRandom();
            if (random.nextInt(5) == 0) {
                this.randomTick(state, level, pos, random);
                return false;
            }
            level.scheduleTick(pos, this, 4);
        }
        return level.setBlock(pos, state, flags);
    }
}
