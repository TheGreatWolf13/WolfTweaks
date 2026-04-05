package tgw.wolf_tweaks.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(AbstractVillager.class)
public abstract class MixinAbstractVillager extends AgeableMob implements Npc, Merchant, InventoryCarrier {

    public MixinAbstractVillager(EntityType<? extends @NotNull AgeableMob> type, Level level) {
        super(type, level);
    }

    @Redirect(method = "addOffersFromTradeSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootContext$Builder;create(Ljava/util/Optional;)Lnet/minecraft/world/level/storage/loot/LootContext;"))
    private LootContext onAddOffersFromTradeSet(LootContext.Builder builder, Optional<Identifier> randomSequenceKey) {
        return builder.withOptionalRandomSeed(this.uuid.getLeastSignificantBits() + this.uuid.getMostSignificantBits()).create(randomSequenceKey);
    }
}
