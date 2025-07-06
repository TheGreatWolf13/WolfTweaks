package tgw.wolf_tweaks.patches;

import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public interface PatchVaultServerData {

    List<ItemStack> itemsToEject();

    Object2LongLinkedOpenHashMap<UUID> rewardedPlayers_();

    void setMap(Object2LongLinkedOpenHashMap<UUID> map);

    long stateUpdatingResumesAt();

    int totalEjectionsNeeded();

    void updateRewardedPlayers(ServerLevel level);
}
