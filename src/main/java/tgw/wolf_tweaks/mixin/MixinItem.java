package tgw.wolf_tweaks.mixin;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class MixinItem implements FeatureElement, ItemLike, FabricItem {

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    public void inject_useOn(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = useOnContext.getItemInHand();
        if (stack.is(ItemTags.SWORDS)) {
            ItemEnchantments enchantments = stack.getEnchantments();
            Level level = useOnContext.getLevel();
            Registry<Enchantment> registry = level.registryAccess().lookup(Registries.ENCHANTMENT).orElse(null);
            if (registry != null) {
                Holder.Reference<Enchantment> enchantment = registry.get(Enchantments.FIRE_ASPECT).orElse(null);
                if (enchantment != null && enchantments.getLevel(enchantment) > 0) {
                    BlockPos pos = useOnContext.getClickedPos();
                    BlockState state = level.getBlockState(pos);
                    if (state.is(BlockTags.CAMPFIRES) || state.is(BlockTags.CANDLES) || state.is(BlockTags.CANDLE_CAKES)) {
                        if (state.hasProperty(BlockStateProperties.LIT) && !state.getValue(BlockStateProperties.LIT)) {
                            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, true));
                            Player player = useOnContext.getPlayer();
                            if (player != null) {
                                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                                stack.hurtAndBreak(1, player, useOnContext.getHand());
                            }
                            level.playLocalSound(pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                            cir.setReturnValue(InteractionResult.SUCCESS);
                        }
                    }
                    else if (state.is(Blocks.TNT)) {
                        Player player = useOnContext.getPlayer();
                        if (TntBlock.prime(level, pos, player)) {
                            level.removeBlock(pos, false);
                        }
                        if (player != null) {
                            player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                            stack.hurtAndBreak(1, player, useOnContext.getHand());
                        }
                        level.playLocalSound(pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0f, 1.0f, false);
                        cir.setReturnValue(InteractionResult.SUCCESS);
                    }
                }
            }
        }
    }
}
