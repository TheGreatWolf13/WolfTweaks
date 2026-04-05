package tgw.wolf_tweaks.mixin;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TemptGoal.class)
public abstract class MixinTemptGoal extends Goal {

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "stop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/TemptGoal;reducedTickDelay(I)I"))
    private int onStop(int i) {
        return 0;
    }
}
