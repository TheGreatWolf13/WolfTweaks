package tgw.wolf_tweaks.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.golem.AbstractGolem;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.wolf_tweaks.patches.PatchCopperGolem;

import java.util.Optional;

@Mixin(CopperGolem.class)
public abstract class MixinCopperGolem extends AbstractGolem implements ContainerUser, Shearable, PatchCopperGolem {

    @Unique private Item lastCarriedItem = Items.AIR;

    public MixinCopperGolem(EntityType<? extends @NotNull AbstractGolem> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addAdditionalSaveData_tail(ValueOutput out, CallbackInfo ci) {
        if (this.lastCarriedItem != Items.AIR) {
            out.store("last_carried_item", Item.CODEC, this.lastCarriedItem.builtInRegistryHolder());
        }
    }

    @Override
    public Item getLastCarriedItem() {
        return this.lastCarriedItem;
    }

    @Inject(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/BehaviorUtils;throwItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/phys/Vec3;)V"))
    private void mobInteract_throwItem(Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        this.lastCarriedItem = Items.AIR;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAdditionalSaveData_tail(ValueInput in, CallbackInfo ci) {
        Optional<Holder<@NotNull Item>> dumbOptional = in.read("last_carried_item", Item.CODEC);
        if (dumbOptional.isPresent()) {
            this.lastCarriedItem = dumbOptional.get().value();
        }
        else {
            this.lastCarriedItem = Items.AIR;
        }
    }

    @Override
    public void setLastCarriedItem(ItemStack stack) {
        this.lastCarriedItem = stack.getItem();
    }
}
