package tgw.wolf_tweaks.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public enum MossyBlocks {
    COBBLESTONE(Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE),
    COBBLESTONE_STAIRS(Blocks.COBBLESTONE_STAIRS, Blocks.MOSSY_COBBLESTONE_STAIRS),
    COBBLESTONE_SLAB(Blocks.COBBLESTONE_SLAB, Blocks.MOSSY_COBBLESTONE_SLAB),
    COBBLESTONE_WALL(Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE_WALL),
    STONE_BRICKS(Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS),
    STONE_BRICK_STAIRS(Blocks.STONE_BRICK_STAIRS, Blocks.MOSSY_STONE_BRICK_STAIRS),
    STONE_BRICK_SLAB(Blocks.STONE_BRICK_SLAB, Blocks.MOSSY_STONE_BRICK_SLAB),
    STONE_BRICK_WALL(Blocks.STONE_BRICK_WALL, Blocks.MOSSY_STONE_BRICK_WALL);

    public static final MossyBlocks[] VALUES = values();
    public final Block block;
    public final Block mossyBlock;

    MossyBlocks(Block block, Block mossyBlock) {
        this.block = block;
        this.mossyBlock = mossyBlock;
    }

    public static @Nullable MossyBlocks anyMatch(Block block) {
        for (MossyBlocks value : VALUES) {
            if (value.block == block) {
                return value;
            }
        }
        return null;
    }

    public static boolean isMoss(Block block) {
        for (MossyBlocks value : VALUES) {
            if (block == value.mossyBlock) {
                return true;
            }
        }
        return false;
    }
}
