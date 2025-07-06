package tgw.wolf_tweaks.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.wolf_tweaks.WolfTweaksClient;

import java.util.List;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem extends Item {

    public MixinBlockItem(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "appendHoverText", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V"))
    private void appendHoverText_appendHoverText(Block block, ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flags) {
        block.appendHoverText(stack, context, list, flags);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            WolfTweaksClient.appendMiningSpeed(block, list);
        }
    }
}
