package tgw.wolf_tweaks.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ReputationEventHandler;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Villager.class)
public abstract class MixinVillager extends AbstractVillager implements ReputationEventHandler, VillagerDataHolder {

    public MixinVillager(EntityType<? extends AbstractVillager> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void die_head(DamageSource damageSource, CallbackInfo ci) {
        Level level = this.level();
        if (!level.isClientSide) {
            //noinspection DataFlowIssue
            List<ServerPlayer> players = level.getServer().getPlayerList().getPlayers();
            if (!players.isEmpty()) {
                String dim;
                ResourceKey<Level> dimension = level.dimension();
                if (dimension == Level.OVERWORLD) {
                    dim = "Overworld";
                }
                else if (dimension == Level.NETHER) {
                    dim = "Nether";
                }
                else if (dimension == Level.END) {
                    dim = "End";
                }
                else {
                    dim = "Unknown";
                }
                BlockPos pos = this.blockPosition();
                Component deathMessage = damageSource.getLocalizedDeathMessage(this).copy().append(" [" + dim + " @ " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + "]");
                for (int i = 0, len = players.size(); i < len; ++i) {
                    ServerPlayer player = players.get(i);
                    player.sendSystemMessage(deathMessage);
                }
            }
        }
    }
}
