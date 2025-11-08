package tgw.wolf_tweaks.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.wolf_tweaks.patches.PatchCopperGolem;

import java.util.Iterator;
import java.util.Map;
import java.util.function.BiConsumer;

@Mixin(TransportItemsBetweenContainers.class)
public abstract class MixinTransportItemsBetweenContainers extends Behavior<PathfinderMob> {

    @Shadow @Final private static int TRANSPORTED_ITEM_MAX_STACK_SIZE;

    public MixinTransportItemsBetweenContainers(Map<MemoryModuleType<?>, MemoryStatus> map) {
        super(map);
    }

    @Unique
    private static ItemStack smartPickupItemFromContainer(Container container, Item lastItem) {
        int i = 0;
        int notFoundCount = -1;
        int notFoundSlot = -1;
        for (Iterator<ItemStack> it = container.iterator(); it.hasNext(); ++i) {
            ItemStack itemStack = it.next();
            if (!itemStack.isEmpty()) {
                if (lastItem == Items.AIR || itemStack.is(lastItem)) {
                    int count = Math.min(itemStack.getCount(), TRANSPORTED_ITEM_MAX_STACK_SIZE);
                    return container.removeItem(i, count);
                }
                if (notFoundSlot < 0) {
                    notFoundSlot = i;
                    notFoundCount = Math.min(itemStack.getCount(), TRANSPORTED_ITEM_MAX_STACK_SIZE);
                }
            }
        }
        if (notFoundSlot >= 0) {
            return container.removeItem(notFoundSlot, notFoundCount);
        }
        return ItemStack.EMPTY;
    }

    @Shadow
    protected abstract void clearMemoriesAfterMatchingTargetFound(PathfinderMob pathfinderMob);

    @ModifyArg(method = "onReachedTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/TransportItemsBetweenContainers;doReachedTargetInteraction(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/Container;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;)V"), index = 3)
    private BiConsumer<PathfinderMob, Container> onReachedTarget_doReachedTargetInteraction_lambda3_failPickUp(BiConsumer<PathfinderMob, Container> biConsumer, @Local(argsOnly = true) TransportItemsBetweenContainers.TransportItemTarget target) {
        return (mob, container) -> {
            this.stopTargetingCurrentTarget(mob);
            this.setVisitedBlockPos(mob, mob.level(), target.pos());
        };
    }

    @ModifyArg(method = "onReachedTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/TransportItemsBetweenContainers;doReachedTargetInteraction(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/Container;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;)V"), index = 5)
    private BiConsumer<PathfinderMob, Container> onReachedTarget_doReachedTargetInteraction_lambda5_failPutDown(BiConsumer<PathfinderMob, Container> biConsumer, @Local(argsOnly = true) TransportItemsBetweenContainers.TransportItemTarget target) {
        return (mob, container) -> {
            this.stopTargetingCurrentTarget(mob);
            this.setVisitedBlockPos(mob, mob.level(), target.pos());
        };
    }

    @Inject(method = "pickUpItems", at = @At("HEAD"), cancellable = true)
    private void pickUpItems_head(PathfinderMob mob, Container container, CallbackInfo ci) {
        if (mob instanceof PatchCopperGolem golem) {
            Item lastCarriedItem = golem.getLastCarriedItem();
            ItemStack carriedItem = smartPickupItemFromContainer(container, lastCarriedItem);
            mob.setItemSlot(EquipmentSlot.MAINHAND, carriedItem);
            mob.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            container.setChanged();
            if (lastCarriedItem == Items.AIR || !carriedItem.is(lastCarriedItem)) {
                this.clearMemoriesAfterMatchingTargetFound(mob);
                golem.setLastCarriedItem(carriedItem);
            }
            ci.cancel();
        }
    }

    @Redirect(method = "putDownItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/TransportItemsBetweenContainers;clearMemoriesAfterMatchingTargetFound(Lnet/minecraft/world/entity/PathfinderMob;)V"))
    private void putDownItem_clearMemoriesAfterMatchingTargetFound(TransportItemsBetweenContainers instance, PathfinderMob pathfinderMob) {
        //Do nothing
    }

    @Shadow
    protected abstract void setVisitedBlockPos(PathfinderMob pathfinderMob, Level level, BlockPos blockPos);

    @Shadow
    protected abstract void stopTargetingCurrentTarget(PathfinderMob pathfinderMob);

    @Redirect(method = "updateInvalidTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/TransportItemsBetweenContainers;setVisitedBlockPos(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V"))
    private void updateInvalidTarget_setVisitedBlockPos(TransportItemsBetweenContainers instance, PathfinderMob pathfinderMob, Level level, BlockPos blockPos) {
        //Do nothing
    }
}
