package tgw.wolf_tweaks.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShovelItem.class)
public abstract class MixinShovelItem extends Item {

    public MixinShovelItem(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/context/UseOnContext;getClickedFace()Lnet/minecraft/core/Direction;"), cancellable = true)
    private void useOn_getClickedFace(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        Player player = context.getPlayer();
        if (player != null && player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof BlockItem) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
