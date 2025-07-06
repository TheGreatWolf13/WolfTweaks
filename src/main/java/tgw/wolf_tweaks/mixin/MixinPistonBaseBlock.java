package tgw.wolf_tweaks.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PistonBaseBlock.class)
public abstract class MixinPistonBaseBlock extends DirectionalBlock {

    public MixinPistonBaseBlock(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/context/BlockPlaceContext;getNearestLookingDirection()Lnet/minecraft/core/Direction;"))
    private Direction getStateForPlacement_getNearestLookingDirection(BlockPlaceContext context) {
        return context.isSecondaryUseActive() ? context.getNearestLookingDirection().getOpposite() : context.getNearestLookingDirection();
    }
}
