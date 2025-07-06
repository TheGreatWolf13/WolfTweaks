package tgw.wolf_tweaks.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.wolf_tweaks.WolfTweaks;

@Mixin(ItemEntity.class)
public abstract class MixinItemEntity extends Entity implements TraceableEntity {

    public MixinItemEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "<init>(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextDouble()D"))
    private static double _init_nextDouble(RandomSource random) {
        if (WolfTweaks.isDroppingScaffolding) {
            return 0.5;
        }
        return random.nextDouble();
    }
}
