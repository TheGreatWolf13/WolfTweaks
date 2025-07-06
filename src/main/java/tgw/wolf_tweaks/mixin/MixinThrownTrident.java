package tgw.wolf_tweaks.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ThrownTrident.class)
public abstract class MixinThrownTrident extends AbstractArrow {

    @Shadow @Final private static EntityDataAccessor<Byte> ID_LOYALTY;

    @Shadow private boolean dealtDamage;

    public MixinThrownTrident(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void onBelowWorld() {
        int loyalty = this.entityData.get(ID_LOYALTY);
        if (loyalty > 0) {
            this.dealtDamage = true;
        }
        else {
            super.onBelowWorld();
        }
    }
}
