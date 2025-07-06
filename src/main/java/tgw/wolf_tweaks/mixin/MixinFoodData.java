package tgw.wolf_tweaks.mixin;

import net.minecraft.world.food.FoodData;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FoodData.class)
public abstract class MixinFoodData {

    @SuppressWarnings("MethodMayBeStatic")
    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/food/FoodData;foodLevel:I", opcode = Opcodes.GETFIELD, ordinal = 2))
    private int onTick(FoodData instance) {
        return 20;
    }

    @SuppressWarnings("MethodMayBeStatic")
    @ModifyConstant(method = "tick", constant = @Constant(intValue = 18))
    private int onTick(int oldValue) {
        return 7;
    }
}
