package tgw.wolf_tweaks.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PhantomSpawner.class)
public abstract class MixinPhantomSpawner implements CustomSpawner {

    @Shadow private int nextTick;

    @Inject(method = "tick", at = @At(value = "RETURN", ordinal = 4))
    private void onTick(ServerLevel serverLevel, boolean bl, boolean bl2, CallbackInfoReturnable<Integer> cir) {
        int add = 60 * 20 * cir.getReturnValue();
        if (add > 10 * 60 * 20) {
            add = 10 * 60 * 20;
        }
        this.nextTick += add;
    }
}
