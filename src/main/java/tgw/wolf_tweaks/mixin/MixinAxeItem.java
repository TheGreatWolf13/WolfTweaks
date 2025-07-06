package tgw.wolf_tweaks.mixin;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AxeItem.class)
public abstract class MixinAxeItem extends DiggerItem {

    public MixinAxeItem(ToolMaterial toolMaterial, TagKey<Block> tagKey, float f, float g, Properties properties) {
        super(toolMaterial, tagKey, f, g, properties);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }
}
