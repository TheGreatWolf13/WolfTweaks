package tgw.wolf_tweaks.mixin;

import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.minecraft.advancements.criterion.ItemDurabilityTrigger;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.wolf_tweaks.WolfTweaks;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements DataComponentHolder, FabricItemStack {

    @Redirect(method = "applyDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/criterion/ItemDurabilityTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;I)V"))
    private void applyDamage_trigger(ItemDurabilityTrigger trigger, ServerPlayer player, ItemStack stack, int damage) {
        trigger.trigger(player, stack, damage);
        if (damage >= 0.9 * this.getMaxDamage()) {
            WolfTweaks.nofityDurability((ItemStack) (Object) this, player);
        }
    }

    @Shadow
    public abstract int getDamageValue();

    @Shadow
    public abstract Component getDisplayName();

    @Shadow
    public abstract int getMaxDamage();

    @Shadow
    public abstract boolean isDamageableItem();

//    @Redirect(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
//    private InteractionResult useOn_useOn(Item item, UseOnContext context) {
//        Player player = context.getPlayer();
//        if (player != null && this.isDamageableItem() && this.getDamageValue() >= 0.9 * this.getMaxDamage()) {
//            WolfTweaks.nofityDurability((ItemStack) (Object) this, player);
//        }
//        return item.useOn(context);
//    }
//
//    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;"))
//    private InteractionResult use_use(Item item, Level level, Player player, InteractionHand hand) {
//        if (this.isDamageableItem() && this.getDamageValue() >= 0.9 * this.getMaxDamage()) {
//            WolfTweaks.nofityDurability((ItemStack) (Object) this, player);
//        }
//        return item.use(level, player, hand);
//    }
}
