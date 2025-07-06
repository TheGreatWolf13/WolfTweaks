package tgw.wolf_tweaks.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PatrollingMonster.class)
public abstract class MixinPatrollingMonster extends Monster {

    public MixinPatrollingMonster(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyConstant(method = "checkPatrollingMonsterSpawnRules", constant = @Constant(intValue = 8))
    private static int onCheckPatrollingMonsterSpawnRules(int constant) {
        return 0;
    }
}
