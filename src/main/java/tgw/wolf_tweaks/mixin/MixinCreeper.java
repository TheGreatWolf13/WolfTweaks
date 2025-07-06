package tgw.wolf_tweaks.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Creeper.class)
public abstract class MixinCreeper extends Monster {

    @Shadow private int droppedSkulls;

    public MixinCreeper(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "canDropMobsSkull", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/monster/Creeper;droppedSkulls:I"))
    private int canDropMobsSkull_droppedSkulls(Creeper instance) {
        return 0;
    }

    @Redirect(method = "canDropMobsSkull", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Creeper;isPowered()Z"))
    private boolean canDropMobsSkull_isPowered(Creeper instance) {
        float chance = this.droppedSkulls == 0 ? 1.0f : Math.max(1.0f / this.droppedSkulls, 0.025f);
        return this.isPowered() && this.random.nextFloat() < chance;
    }

    @Shadow
    public abstract boolean isPowered();
}
