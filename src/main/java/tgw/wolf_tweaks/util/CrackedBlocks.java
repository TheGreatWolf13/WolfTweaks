package tgw.wolf_tweaks.util;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public enum CrackedBlocks {
    STONE_BRICKS(Blocks.STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS),
    NETHER_BRICKS(Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS),
    DEEPSLATE_BRICKS(Blocks.DEEPSLATE_BRICKS, Blocks.CRACKED_DEEPSLATE_BRICKS),
    DEEPSLATE_TILES(Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_TILES),
    POLISHED_BLACKSTONE_BRICKS(Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);

    public static final CrackedBlocks[] VALUES = values();
    public final Block block;
    public final Block cracked;

    CrackedBlocks(Block block, Block cracked) {
        this.block = block;
        this.cracked = cracked;
    }

    public static @Nullable CrackedBlocks anyMatch(Block block) {
        for (CrackedBlocks value : VALUES) {
            if (value.block == block) {
                return value;
            }
        }
        return null;
    }
}
