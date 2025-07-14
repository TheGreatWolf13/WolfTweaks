package tgw.wolf_tweaks.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.wolf_tweaks.WolfTweaksClient;

import java.util.function.Consumer;

@Mixin(BlockItem.class)
public abstract class MixinBlockItem extends Item {

    @SuppressWarnings("DeprecatedIsStillUsed") @Shadow @Final @Deprecated private Block block;

    public MixinBlockItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, TooltipDisplay tooltipDisplay, Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            WolfTweaksClient.appendMiningSpeed(this.block, consumer);
        }
    }
}
