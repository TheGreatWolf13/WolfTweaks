package tgw.wolf_tweaks.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Creeper.class)
public abstract class MixinCreeper extends Monster {

    @Unique private int numberOfDroppedSkulls;

    public MixinCreeper(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract boolean isPowered();

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "killedEntity", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/world/entity/monster/Creeper;droppedSkulls:Z"))
    private boolean killedEntity_droppedSkulls_read(Creeper instance) {
        return false;
    }

    @Redirect(method = "killedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Creeper;isPowered()Z"))
    private boolean killedEntity_isPowered(Creeper instance) {
        float chance = this.numberOfDroppedSkulls == 0 ? 1.0f : Math.max(1.0f / this.numberOfDroppedSkulls, 0.025f);
        return this.isPowered() && this.random.nextFloat() < chance;
    }

    @Redirect(method = "method_72496", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/world/entity/monster/Creeper;droppedSkulls:Z"))
    private void killedEntity_lambda_droppedSkulls_write(Creeper instance, boolean value) {
        ++this.numberOfDroppedSkulls;
    }
}
