package tgw.wolf_tweaks.mixin;

import com.mojang.blaze3d.platform.WindowEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.wolf_tweaks.WolfTweaks;
import tgw.wolf_tweaks.WolfTweaksClient;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler {

    @Shadow public @Nullable LocalPlayer player;

    public MixinMinecraft(String string) {
        super(string);
    }

    @Redirect(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isItemEnabled(Lnet/minecraft/world/flag/FeatureFlagSet;)Z"))
    private boolean startAttack_isItemEnabled(ItemStack stack, FeatureFlagSet flags) {
        if (this.player != null && stack.isDamageableItem() && stack.getDamageValue() >= 0.9 * stack.getMaxDamage()) {
            WolfTweaks.nofityDurability(stack, this.player);
        }
        return stack.isItemEnabled(flags);
    }

    @Redirect(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isItemEnabled(Lnet/minecraft/world/flag/FeatureFlagSet;)Z"))
    private boolean startUseItem_isItemEnabled(ItemStack stack, FeatureFlagSet flags) {
        if (this.player != null && stack.isDamageableItem() && stack.getDamageValue() >= 0.9 * stack.getMaxDamage()) {
            WolfTweaks.nofityDurability(stack, this.player);
        }
        return stack.isItemEnabled(flags);
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;useItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult startUseItem_useItemOn(MultiPlayerGameMode gamemode, LocalPlayer player, InteractionHand hand, BlockHitResult blockHit) {
        boolean placingBlock = false;
        if (hand == InteractionHand.MAIN_HAND) {
            placingBlock = player.getMainHandItem().getItem() instanceof BlockItem;
        }
        InteractionResult result = gamemode.useItemOn(player, hand, blockHit);
        if (placingBlock && result instanceof InteractionResult.Success) {
            WolfTweaksClient.placedBlock = true;
        }
        return result;
    }
}
