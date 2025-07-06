package tgw.wolf_tweaks.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Inventory.class)
public abstract class MixinInventory implements Container, Nameable {

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "getSuitableHotbarSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEnchanted()Z"))
    private boolean getSuitableHotbarSlot_isEnchanted(ItemStack stack) {
        return stack.isEnchanted() || stack.isEnchantable();
    }
}
