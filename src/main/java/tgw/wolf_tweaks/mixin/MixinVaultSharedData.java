package tgw.wolf_tweaks.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.wolf_tweaks.patches.PatchVaultServerData;

@Mixin(VaultSharedData.class)
public abstract class MixinVaultSharedData {

    @SuppressWarnings("MethodMayBeStatic")
    @Inject(method = "updateConnectedPlayersWithinRange", at = @At("HEAD"))
    private void updateConnectedPlayersWithinRange_head(ServerLevel level, BlockPos pos, VaultServerData data, VaultConfig config, double distance, CallbackInfo ci) {
        ((PatchVaultServerData) data).updateRewardedPlayers(level);
    }
}
