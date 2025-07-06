package tgw.wolf_tweaks.mixin;

import net.fabricmc.fabric.api.block.v1.FabricBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.wolf_tweaks.WolfTweaks;

@Mixin(Block.class)
public abstract class MixinBlock extends BlockBehaviour implements ItemLike, FabricBlock {

    public MixinBlock(Properties properties) {
        super(properties);
    }

    @Redirect(method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;getX()I"))
    private static int popResource_getX(BlockPos pos) {
        if (WolfTweaks.isDroppingScaffolding) {
            return WolfTweaks.scaffoldingDropX;
        }
        return pos.getX();
    }

    @Redirect(method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;getZ()I"))
    private static int popResource_getZ(BlockPos pos) {
        if (WolfTweaks.isDroppingScaffolding) {
            return WolfTweaks.scaffoldingDropZ;
        }
        return pos.getZ();
    }
}
