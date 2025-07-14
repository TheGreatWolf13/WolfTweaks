package tgw.wolf_tweaks.mixin;

import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.levelgen.PhantomSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PhantomSpawner.class)
public abstract class MixinPhantomSpawner implements CustomSpawner {
    
    @SuppressWarnings("MethodMayBeStatic")
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 60))
    private int onTick(int constant) {
        return 4 * constant;
    }
}
