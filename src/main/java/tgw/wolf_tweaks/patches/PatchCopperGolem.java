package tgw.wolf_tweaks.patches;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface PatchCopperGolem {

    Item getLastCarriedItem();

    void setLastCarriedItem(ItemStack stack);
}
