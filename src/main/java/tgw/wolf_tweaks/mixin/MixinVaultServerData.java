package tgw.wolf_tweaks.mixin;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.wolf_tweaks.patches.PatchVaultServerData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mixin(VaultServerData.class)
public abstract class MixinVaultServerData implements PatchVaultServerData {

    @Shadow static Codec<VaultServerData> CODEC;
    @Shadow @Final private List<ItemStack> itemsToEject;
    @Unique private final Object2LongLinkedOpenHashMap<UUID> rewardedPlayers_ = new Object2LongLinkedOpenHashMap<>();
    @Shadow private long stateUpdatingResumesAt;
    @Shadow private int totalEjectionsNeeded;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void _clinit_tail(CallbackInfo ci) {
        Codec<Object2LongLinkedOpenHashMap<UUID>> codecLinkedMap = RecordCodecBuilder.create(instance -> instance.group(UUIDUtil.CODEC.listOf().fieldOf("uuids").forGetter(map -> new ArrayList<>(map.keySet())),
                                                                                                                        Codec.LONG.listOf().fieldOf("times").forGetter(map -> new ArrayList<>(map.values())))
                                                                                                                 .apply(instance, (uuids, times) -> {
                                                                                                                     Object2LongLinkedOpenHashMap<UUID> map = new Object2LongLinkedOpenHashMap<>();
                                                                                                                     for (int i = 0, len = uuids.size(); i < len; ++i) {
                                                                                                                         map.put(uuids.get(i), times.get(i));
                                                                                                                     }
                                                                                                                     return map;
                                                                                                                 }));
        CODEC = RecordCodecBuilder.create(instance -> instance.group(codecLinkedMap.lenientOptionalFieldOf("rewarded_players", new Object2LongLinkedOpenHashMap<>()).forGetter(data -> ((PatchVaultServerData) data).rewardedPlayers_()),
                                                                     Codec.LONG.lenientOptionalFieldOf("state_updating_resumes_at", 0L).forGetter(data -> ((PatchVaultServerData) data).stateUpdatingResumesAt()),
                                                                     ItemStack.CODEC.listOf().lenientOptionalFieldOf("items_to_eject", List.of()).forGetter(data -> ((PatchVaultServerData) data).itemsToEject()),
                                                                     Codec.INT.lenientOptionalFieldOf("total_ejections_needed", 0).forGetter(data -> ((PatchVaultServerData) data).totalEjectionsNeeded()))
                                                              .apply(instance, (map, resumesAt, items, ejections) -> {
                                                                  VaultServerData data = new VaultServerData(Set.of(), resumesAt, items, ejections);
                                                                  ((PatchVaultServerData) data).setMap(map);
                                                                  return data;
                                                              }));
    }

    /**
     * @author TheGreatWolf
     * @reason Also store the time.
     */
    @VisibleForTesting
    @Overwrite
    public void addToRewardedPlayers(Player player) {
        this.rewardedPlayers_.put(player.getUUID(), player.level().getGameTime());
        if (this.rewardedPlayers_.size() > 128) {
            this.rewardedPlayers_.removeFirstLong();
        }
        this.markChanged();
    }

    /**
     * @author TheGreatWolf
     * @reason Access the new collection.
     */
    @Overwrite
    public Set<UUID> getRewardedPlayers() {
        return this.rewardedPlayers_.keySet();
    }

    /**
     * @author TheGreatWolf
     * @reason Access the new collection.
     */
    @Overwrite
    public boolean hasRewardedPlayer(Player player) {
        long time = this.rewardedPlayers_.getOrDefault(player.getUUID(), -1);
        if (time == -1) {
            return false;
        }
        long currentTime = player.level().getGameTime();
        if (currentTime - time >= 15 * 60 * 20) {
            this.rewardedPlayers_.removeLong(player.getUUID());
            return false;
        }
        return true;
    }

    @Override
    public List<ItemStack> itemsToEject() {
        return this.itemsToEject;
    }

    @Shadow
    protected abstract void markChanged();

    @Override
    public Object2LongLinkedOpenHashMap<UUID> rewardedPlayers_() {
        return this.rewardedPlayers_;
    }

    /**
     * @author TheGreatWolf
     * @reason Access the new collection.
     */
    @Overwrite
    public void set(VaultServerData vaultServerData) {
        PatchVaultServerData data = (PatchVaultServerData) vaultServerData;
        this.stateUpdatingResumesAt = data.stateUpdatingResumesAt();
        this.itemsToEject.clear();
        this.itemsToEject.addAll(data.itemsToEject());
        this.rewardedPlayers_.clear();
        this.rewardedPlayers_.putAll(data.rewardedPlayers_());
    }

    @Override
    public void setMap(Object2LongLinkedOpenHashMap<UUID> map) {
        this.rewardedPlayers_.putAll(map);
    }

    @Override
    public long stateUpdatingResumesAt() {
        return this.stateUpdatingResumesAt;
    }

    @Override
    public int totalEjectionsNeeded() {
        return this.totalEjectionsNeeded;
    }

    @Override
    public void updateRewardedPlayers(ServerLevel level) {
        long currentTime = level.getGameTime();
        for (ObjectBidirectionalIterator<Object2LongMap.Entry<UUID>> it = this.rewardedPlayers_.object2LongEntrySet().fastIterator(); it.hasNext(); ) {
            Object2LongMap.Entry<UUID> entry = it.next();
            if (currentTime - entry.getLongValue() >= 15 * 60 * 20) {
                it.remove();
            }
        }
    }
}
