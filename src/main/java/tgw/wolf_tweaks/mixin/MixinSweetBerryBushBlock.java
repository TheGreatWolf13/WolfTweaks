package tgw.wolf_tweaks.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SweetBerryBushBlock.class)
public abstract class MixinSweetBerryBushBlock extends VegetationBlock implements BonemealableBlock {

    public MixinSweetBerryBushBlock(Properties properties) {
        super(properties);
    }

    @Unique
    private static boolean shouldBeAffected(Entity entity) {
        if (entity.isCrouching() || entity instanceof LivingEntity living && living.hasItemInSlot(EquipmentSlot.LEGS)) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (entity instanceof Player player && player.isCreative()) {
            return false;
        }
        return true;
    }

    @SuppressWarnings({"MethodMayBeStatic"})
    @WrapOperation(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean onEntityInside_damage(Entity entity, ServerLevel level, DamageSource damageSource, float amount, Operation<Boolean> original) {
        if (!shouldBeAffected(entity)) {
            return false;
        }
        return original.call(entity, level, damageSource, amount);
    }

    @SuppressWarnings({"MethodMayBeStatic"})
    @WrapOperation(method = "entityInside", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;makeStuckInBlock(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/Vec3;)V"))
    private void onEntityInside_stuck(Entity entity, BlockState blockState, Vec3 speedMultiplier, Operation<Void> original) {
        if (!shouldBeAffected(entity)) {
            return;
        }
        original.call(entity, blockState, speedMultiplier);
    }
}
