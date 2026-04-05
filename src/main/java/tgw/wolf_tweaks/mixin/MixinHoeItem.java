package tgw.wolf_tweaks.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(HoeItem.class)
public abstract class MixinHoeItem extends Item {

    @Shadow @Final protected static Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES;

    public MixinHoeItem(Properties properties) {
        super(properties);
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void _clinit_(CallbackInfo ci) {
        TILLABLES.put(Blocks.PODZOL, Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(Blocks.FARMLAND.defaultBlockState())));
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void onUseOn(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof CropBlock crop) {
            if (crop.getAge(state) == crop.getMaxAge()) {
                if (!level.isClientSide()) {
                    List<ItemStack> drops = Block.getDrops(state, (ServerLevel) level, pos, null, context.getPlayer(), context.getItemInHand());
                    Item seed = crop.asItem();
                    boolean foundSeed = false;
                    for (int i = 0, len = drops.size(); i < len; i++) {
                        ItemStack stack = drops.get(i);
                        if (!foundSeed && stack.is(seed)) {
                            stack.shrink(1);
                            foundSeed = true;
                        }
                        Block.popResource(level, pos, stack);
                    }
                    if (foundSeed) {
                        level.setBlockAndUpdate(pos, crop.getStateForAge(0));
                        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
                    }
                    else {
                        level.removeBlock(pos, false);
                        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
                    }
                }
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
        else if (state.getBlock() instanceof NetherWartBlock) {
            if (state.getValue(NetherWartBlock.AGE) == NetherWartBlock.MAX_AGE) {
                if (!level.isClientSide()) {
                    List<ItemStack> drops = Block.getDrops(state, (ServerLevel) level, pos, null, context.getPlayer(), context.getItemInHand());
                    Item seed = Blocks.NETHER_WART.asItem();
                    boolean foundSeed = false;
                    for (int i = 0, len = drops.size(); i < len; i++) {
                        ItemStack stack = drops.get(i);
                        if (!foundSeed && stack.is(seed)) {
                            stack.shrink(1);
                            foundSeed = true;
                        }
                        Block.popResource(level, pos, stack);
                    }
                    if (foundSeed) {
                        level.setBlockAndUpdate(pos, Blocks.NETHER_WART.defaultBlockState());
                        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
                    }
                    else {
                        level.removeBlock(pos, false);
                        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
                    }
                }
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }
}
