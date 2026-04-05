package tgw.wolf_tweaks.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AnvilBlock.class)
public abstract class MixinAnvilBlock extends FallingBlock {

    public MixinAnvilBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(@NotNull ItemStack itemStack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        if (itemStack.is(Items.IRON_INGOT)) {
            if (state.is(Blocks.CHIPPED_ANVIL)) {
                level.setBlockAndUpdate(pos, Blocks.ANVIL.withPropertiesOf(state));
                itemStack.consume(1, player);
                player.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
            if (state.is(Blocks.DAMAGED_ANVIL)) {
                level.setBlockAndUpdate(pos, Blocks.CHIPPED_ANVIL.withPropertiesOf(state));
                itemStack.consume(1, player);
                player.playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0f, 1.0f);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(itemStack, state, level, pos, player, hand, hitResult);
    }
}
