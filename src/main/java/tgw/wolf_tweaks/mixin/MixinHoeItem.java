package tgw.wolf_tweaks.mixin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(HoeItem.class)
public abstract class MixinHoeItem extends DiggerItem {

    @Shadow @Final protected static Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> TILLABLES;

    public MixinHoeItem(ToolMaterial toolMaterial, TagKey<Block> tagKey, float f, float g, Properties properties) {
        super(toolMaterial, tagKey, f, g, properties);
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void _clinit_(CallbackInfo ci) {
        TILLABLES.put(Blocks.PODZOL, Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(Blocks.FARMLAND.defaultBlockState())));
    }
}
