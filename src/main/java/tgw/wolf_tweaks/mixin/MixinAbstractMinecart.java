package tgw.wolf_tweaks.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.wolf_tweaks.WolfTweaks;
import tgw.wolf_tweaks.patches.PatchAbstractMinecart;

@Mixin(AbstractMinecart.class)
public abstract class MixinAbstractMinecart extends VehicleEntity implements PatchAbstractMinecart {

    @Unique private boolean isChunkLoader;
    @Unique private @Nullable ChunkPos lastChunkPos;
    @Unique private int particleTicker;

    public MixinAbstractMinecart(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;DDD)V", at = @At("TAIL"))
    private void _init_tail(CallbackInfo callbackInfo) {
        if (this.isChunkLoader) {
            this.startChunkLoader();
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    public void addAdditionalSaveData_return(ValueOutput output, CallbackInfo ci) {
        output.putBoolean("chunkLoader", this.isChunkLoader);
    }

    @Override
    public boolean isChunkLoader() {
        return this.isChunkLoader;
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    public void readAdditionalSaveData_return(ValueInput input, CallbackInfo ci) {
        this.isChunkLoader = input.getBooleanOr("chunkLoader", false);
    }

    @Override
    public void remove(@NotNull Entity.RemovalReason reason) {
        if (this.isChunkLoader) {
            this.stopChunkLoader();
        }
        super.remove(reason);
    }

    @Override
    public void setChunkLoaderName(String name) {
        this.setCustomName(Component.literal(name));
        this.setCustomNameVisible(true);
    }

    @Override
    public void setChunkLoaderNameFromInventory() {
        if ((Object) this instanceof MinecartChest minecartChest) {
            ItemStack firstSlot = minecartChest.getItemStacks().getFirst();
            if (!firstSlot.isEmpty() && firstSlot.get(DataComponents.CUSTOM_NAME) != null) {
                this.setChunkLoaderName(firstSlot.getCustomName().getString());
                return;
            }
        }
        this.setChunkLoaderName("Chunk Loader");
    }

    @Unique
    private void spawnParticles() {
        ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY(), this.getZ(), 1, 0.25, 0.25, 0.25, 0.15f);
    }

    @Override
    public void startChunkLoader() {
        if (this.level().isClientSide()) {
            return;
        }
        this.isChunkLoader = true;
        WolfTweaks.LOGGER.debug("Starting chunk loader in {}", this.level().dimension().identifier());
    }

    @Unique
    private void stopChunkLoader(Boolean keepName) {
        this.isChunkLoader = false;
        WolfTweaks.CHUNK_LOADER_MANAGER.removeChunkLoader(this);
        if (!keepName) {
            this.setCustomName(null);
            this.setCustomNameVisible(false);
        }
    }

    @Override
    public void stopChunkLoader() {
        this.stopChunkLoader(false);
    }

    @Override
    public @Nullable Entity teleport(@NotNull TeleportTransition teleportTransition) {
        boolean wasChunkLoader = this.isChunkLoader;
        if (wasChunkLoader) {
            this.stopChunkLoader(true);
        }
        Entity newEntity = super.teleport(teleportTransition);
        if (wasChunkLoader && newEntity != null) {
            ((PatchAbstractMinecart) newEntity).startChunkLoader();
        }
        return newEntity;
    }

    @Unique
    private void tickParticles() {
        this.particleTicker += 1;
        if (this.particleTicker >= 3) {
            this.particleTicker = 0;
            this.spawnParticles();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void tick_tail(CallbackInfo ci) {
        if (!this.isChunkLoader) {
            return;
        }
        ChunkPos chunkPos = this.chunkPosition();
        if (this.lastChunkPos == null || this.lastChunkPos != chunkPos) {
            this.lastChunkPos = chunkPos;
            WolfTweaks.LOGGER.debug("Re-registering chunk loader");
            WolfTweaks.CHUNK_LOADER_MANAGER.registerChunkLoader(this);
        }
        this.tickParticles();
    }
}
