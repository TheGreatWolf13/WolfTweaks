package tgw.wolf_tweaks.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.wolf_tweaks.patches.PatchAbstractMinecart;

import java.util.List;

@Mixin(DispenserBlock.class)
public abstract class MixinDispenserBlock extends BaseEntityBlock {

    public MixinDispenserBlock(Properties properties) {
        super(properties);
    }

    @Unique
    private static void toggleMinecraftChunkLoader(ServerLevel level, BlockState state, BlockPos pos) {
        BlockPos blockPos = pos.relative(state.getValue(DispenserBlock.FACING));
        List<AbstractMinecart> list = level.getEntitiesOfClass(AbstractMinecart.class, new AABB(blockPos), EntitySelector.ENTITY_STILL_ALIVE);
        for (int i = 0, len = list.size(); i < len; ++i) {
            PatchAbstractMinecart minecart = (PatchAbstractMinecart) list.get(i);
            if (minecart.isChunkLoader()) {
                minecart.stopChunkLoader();
            }
            else {
                minecart.startChunkLoader();
                minecart.setChunkLoaderNameFromInventory();
            }
        }
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Inject(at = @At("HEAD"), method = "dispenseFrom", cancellable = true)
    private void dispenseFrom_head(ServerLevel level, BlockState state, BlockPos pos, CallbackInfo info) {
        if (level.isClientSide) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof DispenserBlockEntity te)) {
            return;
        }
        if (!te.getItem(0).is(Items.OXIDIZED_COPPER) || !te.getItem(1).is(Items.AMETHYST_BLOCK) || !te.getItem(2).is(Items.OXIDIZED_COPPER)) {
            return;
        }
        if (!te.getItem(3).is(Items.AMETHYST_BLOCK) || !te.getItem(4).is(Items.GLOWSTONE) || !te.getItem(5).is(Items.AMETHYST_BLOCK)) {
            return;
        }
        if (!te.getItem(6).is(Items.OXIDIZED_COPPER) || !te.getItem(7).is(Items.AMETHYST_BLOCK) || !te.getItem(8).is(Items.OXIDIZED_COPPER)) {
            return;
        }
        toggleMinecraftChunkLoader(level, state, pos);
        level.levelEvent(LevelEvent.SOUND_DISPENSER_DISPENSE, pos, 0);
        level.gameEvent(GameEvent.BLOCK_ACTIVATE, pos, GameEvent.Context.of(te.getBlockState()));
        info.cancel();
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "getStateForPlacement", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/context/BlockPlaceContext;getNearestLookingDirection()Lnet/minecraft/core/Direction;"))
    private Direction getStateForPlacement_getNearestLookingDirection(BlockPlaceContext context) {
        return context.isSecondaryUseActive() ? context.getNearestLookingDirection().getOpposite() : context.getNearestLookingDirection();
    }
}
