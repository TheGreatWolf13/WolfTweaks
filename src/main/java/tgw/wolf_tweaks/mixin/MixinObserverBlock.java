package tgw.wolf_tweaks.mixin;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.ObserverBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ObserverBlock.class)
public abstract class MixinObserverBlock extends DirectionalBlock {

    public MixinObserverBlock(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/context/BlockPlaceContext;getNearestLookingDirection()Lnet/minecraft/core/Direction;"))
    private Direction getStateForPlacement_getNearestLookingDirection(BlockPlaceContext context) {
        return context.isSecondaryUseActive() ? context.getNearestLookingDirection().getOpposite() : context.getNearestLookingDirection();
    }
}
