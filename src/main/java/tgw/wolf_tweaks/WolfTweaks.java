package tgw.wolf_tweaks;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tgw.wolf_tweaks.minecart.ChunkLoaderManager;
import tgw.wolf_tweaks.util.CrackedBlocks;
import tgw.wolf_tweaks.util.MossyBlocks;
import tgw.wolf_tweaks.util.Util;

import java.util.List;

/**
 * <p>Features:</p>
 * <br>
 * Smithing Templates are not consumed in a Smithing Table;<br>
 * Anvils have no use penalty and XP cost is determined by the increase in enchantments and level;<br>
 * Horses, mules, donkeys and llamas have their stats shown in the inventory screen;<br>
 * Vaults now have a 30min cooldown and then can be opened again;<br>
 * Axes only take 1 durability point when used as a weapon;<br>
 * Buttons in the GUI do not get stuck on focused when using the mouse;<br>
 * Food regenerates until the player can no longer run, and saturation regenerates quickly regardless of hunger level, preventing "food camping" and encouraging eating more diverse foods;<br>
 * Music has a shorter delay between tracks;<br>
 * Patrols and illagers only spawn at light level 0 to be consistent with other hostile mobs and make villages safer;<br>
 * Phantoms have a cooldown after spawning;<br>
 * Tridents with loyalty can return from the Void;<br>
 * Jungle leaves drop the same amount of saplings as other leaves;<br>
 * Removed some splashes;<br>
 * Pottery Sherds can be duplicated;<br>
 * Copper and wood can be cut at a Stonecutter;<br>
 * Fast Leaf Decay;<br>
 * Minecarts load chunks;<br>
 * Durability notifier;<br>
 * Impaling also works on entities that are touching water / in rain;<br>
 * Scaffolding drops without scattering;<br>
 * Pick-block should no longer replace tools (unenchanted);<br>
 * Mining speed is now shown on blocks tooltip based on the current held tool and player status;<br>
 * <br>
 * 1.12.0<br>
 * - Villagers now have death messages;<br>
 * <br>
 * 1.13.0<br>
 * - Pistons, Observers, Dropper and Dispensers can be placed in the opposite direction by holding shift;<br>
 * <br>
 * 1.14.0<br>
 * - Added replace block keybind;<br>
 * <br>
 * 1.15.0<br>
 * - Added client command 'packetinfo' to show the amount of packets received by type;<br>
 * <p>1.15.1<br>
 * - Using PacketTypes instead of class names because of obfuscation;<br>
 * <br>
 * 1.16.0<br>
 * - Map to png;<br>
 * - Podzol is now tillable with a hoe;<br>
 * <br>
 * 1.17.0<br>
 * - Added the option to send RGB info to AuroraRGB;<br>
 * - Charged Creepers can make multiple mobs drop their heads, at a diminishing efficiency;<br>
 * - Shovel cannot flatten with Block in offhand anymore;<br>
 * - Deepslate can be stonecut into useful stuff;<br>
 * - Random Block Placement that is actually random;<br>
 * <br>
 * 1.17.1<br>
 * - Reduced vault cooldown to 15min.
 * <br>
 * - Fire Aspect Swords can now light campfires, candles and ignite TNT;<br>
 * <br>
 * - Some blocks can get cracked / mossy with time;<br>
 * - Campfires burn out after some time;<br>
 * <br>
 * 1.22.0<br>
 * - Improved copper golems with better memory
 * <br>
 **/
public class WolfTweaks implements ModInitializer {

    public static final String MOD_ID = "wolf_tweaks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ChunkLoaderManager CHUNK_LOADER_MANAGER = new ChunkLoaderManager();
    private static final long NULL_POS = 0b0111_1111_1111_1111_1111_1111_11_01_1111_1111_1111_1111_1111_1111_0111_1111_1111L;
    private static final Object2ObjectMap<Level, Long2LongMap> SCAFFOLDING_DEPENDENCY = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectMap<Level, Long2IntMap> SCAFFOLDING_REF_COUNT = new Object2ObjectOpenHashMap<>();
    private static final Object2LongMap<Level> SCAFFOLDING_TIME = new Object2LongOpenHashMap<>();
    private static final int DECAY_RADIUS = 64;
    private static final int DECAY_RATE = 3;
    public static boolean isDroppingScaffolding;
    public static boolean preventScaffoldingDoubleTicking;
    public static int scaffoldingDropX;
    public static int scaffoldingDropZ;

    public static void addScaffoldingDependency(Level level, long pos, long dependsOn) {
        Long2LongMap scaffoldingDependency = getDependency(level);
        Long2IntMap scaffoldingRefCount = getRefCount(level);
//        LOGGER.info("Trying to add dependency for [{}, {}, {}]: [{}, {}, {}]", BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos), BlockPos.getX(dependsOn), BlockPos.getY(dependsOn), BlockPos.getZ(dependsOn));
        if (scaffoldingDependency.get(pos) == NULL_POS) {
            if (scaffoldingDependency.get(dependsOn) != pos) {
//                LOGGER.info(" -> Currently doesn't depend on anything, adding");
                scaffoldingDependency.put(pos, dependsOn);
                scaffoldingRefCount.mergeInt(pos, 1, Integer::sum);
//                LOGGER.info(" -> Ref count for [{}, {}, {}] is now {}", BlockPos.getX(pos), BlockPos.getY(pos), BlockPos.getZ(pos), scaffoldingRefCount.get(pos));
                //Recursively add ref counts
                long depends = dependsOn;
                long dependence = 0;
                while (true) {
                    dependence = scaffoldingDependency.get(depends);
                    if (dependence == NULL_POS) {
                        break;
                    }
                    scaffoldingRefCount.mergeInt(depends, 1, Integer::sum);
//                    LOGGER.info(" -> Ref count for [{}, {}, {}] is now {}", BlockPos.getX(depends), BlockPos.getY(depends), BlockPos.getZ(depends), scaffoldingRefCount.get(depends));
                    depends = dependence;
                }
            }
            else {
//                LOGGER.info(" -> NOT adding circular dependence");
            }
        }
        else {
//            LOGGER.info(" -> Already has a dependence, NOT adding");
        }
//        LOGGER.info(" -> Sizes: {}, {}", scaffoldingDependency.size(), scaffoldingRefCount.size());
    }

    public static void findScaffoldingDroppingPos(Level level, BlockPos pos) {
        Long2LongMap scaffoldingDependency = getDependency(level);
        long dropPos = pos.asLong();
//        LOGGER.info("Looking for drop pos for [{}, {}, {}]", BlockPos.getX(dropPos), BlockPos.getY(dropPos), BlockPos.getZ(dropPos));
        while (true) {
            long newPos = scaffoldingDependency.get(dropPos);
            if (newPos == NULL_POS) {
                break;
            }
            dropPos = newPos;
        }
//        LOGGER.info(" -> Found drop pos: [{}, {}, {}]", BlockPos.getX(dropPos), BlockPos.getY(dropPos), BlockPos.getZ(dropPos));
        scaffoldingDropX = BlockPos.getX(dropPos);
        scaffoldingDropZ = BlockPos.getZ(dropPos);
    }

    private static Long2LongMap getDependency(Level level) {
        SCAFFOLDING_TIME.put(level, level.getGameTime());
        Long2LongMap map = SCAFFOLDING_DEPENDENCY.get(level);
        if (map == null) {
            map = new Long2LongOpenHashMap();
            map.defaultReturnValue(NULL_POS);
            SCAFFOLDING_DEPENDENCY.put(level, map);
        }
        return map;
    }

    private static Long2IntMap getRefCount(Level level) {
        SCAFFOLDING_TIME.put(level, level.getGameTime());
        Long2IntMap map = SCAFFOLDING_REF_COUNT.get(level);
        if (map == null) {
            map = new Long2IntOpenHashMap();
            SCAFFOLDING_REF_COUNT.put(level, map);
        }
        return map;
    }

    public static void nofityDurability(ItemStack stack, Player player) {
        player.displayClientMessage(Component.translatable("wolf_tweaks.notify_durability", stack.getDisplayName(), Component.literal("10%").withStyle(ChatFormatting.RED)).withStyle(ChatFormatting.YELLOW), true);
        Level level = player.level();
        level.playSound(level.isClientSide() ? player : null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_PLING, SoundSource.MASTER, 0.6f, 1.0f);
    }

    private static void registerServerEvents() {
        ServerTickEvents.END_WORLD_TICK.register(level -> {
            long time = level.getGameTime();
            if ((time & 511) == 0) {
                long lastTime = SCAFFOLDING_TIME.getLong(level);
                if (lastTime == -1) {
                    return;
                }
                if (time - lastTime >= 400) {
                    getDependency(level).clear();
                    getRefCount(level).clear();
                    SCAFFOLDING_TIME.remove(level);
                }
            }
        });
        ServerTickEvents.END_WORLD_TICK.register(level -> {
            List<ServerPlayer> players = level.players();
            if (players.isEmpty()) {
                return;
            }
            RandomSource random = level.random;
            for (int i = DECAY_RATE; i > 0; --i) {
                ServerPlayer player = players.size() == 1 ? players.getFirst() : players.get(random.nextInt(players.size()));
                int offX = random.nextIntBetweenInclusive(-DECAY_RADIUS, DECAY_RADIUS);
                int yDown = Math.max(level.getMinY(), player.getBlockY() - DECAY_RADIUS);
                int yUp = Math.min(level.getMaxY(), player.getBlockY() + DECAY_RADIUS);
                int y = random.nextIntBetweenInclusive(yDown, yUp);
                int offZ = random.nextIntBetweenInclusive(-DECAY_RADIUS, DECAY_RADIUS);
                BlockPos randomPos = player.blockPosition().offset(offX, 0, offZ).atY(y);
                if (!level.hasChunkAt(randomPos)) {
                    continue;
                }
                //Decay
                BlockState state = level.getBlockState(randomPos);
                tryToDecay(level, state, randomPos);
            }
        });
    }

    public static void removeDependencies(Level level, BlockPos pos) {
        long toDrop = pos.asLong();
        Long2IntMap scaffoldingRefCount = getRefCount(level);
        Long2LongMap scaffoldingDependency = getDependency(level);
//        LOGGER.info("Removing dependency for [{}, {}, {}]", BlockPos.getX(toDrop), BlockPos.getY(toDrop), BlockPos.getZ(toDrop));
        while (toDrop != NULL_POS) {
            long dependence = NULL_POS;
            int refCount = scaffoldingRefCount.get(toDrop) - 1;
            if (refCount <= 0) {
                dependence = scaffoldingDependency.remove(toDrop);
                scaffoldingRefCount.remove(toDrop);
//                LOGGER.info(" -> [{}, {}, {}] is no longer needed, dropping", BlockPos.getX(toDrop), BlockPos.getY(toDrop), BlockPos.getZ(toDrop));
//                LOGGER.info(" -> Sizes: {}, {}", scaffoldingDependency.size(), scaffoldingRefCount.size());
            }
            else {
                dependence = scaffoldingDependency.get(toDrop);
                scaffoldingRefCount.put(toDrop, refCount);
//                LOGGER.info(" -> Ref count for [{}, {}, {}] is now {}", BlockPos.getX(toDrop), BlockPos.getY(toDrop), BlockPos.getZ(toDrop), refCount);
//                LOGGER.info(" -> Sizes: {}, {}", scaffoldingDependency.size(), scaffoldingRefCount.size());
            }
            toDrop = dependence;
        }
//        if (!scaffoldingDependency.isEmpty()) {
//            LOGGER.info(" -> Dependencies: ");
//            for (Long2LongMap.Entry entry : scaffoldingDependency.long2LongEntrySet()) {
//                long p = entry.getLongKey();
//                long d = entry.getLongValue();
//                LOGGER.info("     -> [{}, {}, {}] depends on [{}, {}, {}] with ref count {}", BlockPos.getX(p), BlockPos.getY(p), BlockPos.getZ(p), BlockPos.getX(d), BlockPos.getY(d), BlockPos.getZ(d), scaffoldingRefCount.get(p));
//            }
//        }
    }

    private static void tryToDecay(ServerLevel level, BlockState state, BlockPos pos) {
        RandomSource random = level.random;
        Block block = state.getBlock();
        if (block == Blocks.CAMPFIRE || block == Blocks.SOUL_CAMPFIRE) {
            if (random.nextFloat() < 0.2f) {
                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, false));
            }
            return;
        }
        CrackedBlocks crackedBlocks = CrackedBlocks.anyMatch(block);
        if (crackedBlocks != null) {
            if (random.nextFloat() < 0.02f) {
                boolean exposed = false;
                for (Direction dir : Util.DIRECTIONS) {
                    BlockPos relativePos = pos.relative(dir);
                    if (!level.getBlockState(relativePos).isFaceSturdy(level, relativePos, dir.getOpposite())) {
                        exposed = true;
                        break;
                    }
                }
                if (exposed) {
                    int count = 0;
                    int x = pos.getX();
                    int y = pos.getY();
                    int z = pos.getZ();
                    BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
                    float chance = 1.0f;
                    search:
                    for (int dx = -4; dx <= 4; ++dx) {
                        mutablePos.setX(x + dx);
                        for (int dy = -4; dy <= 4; ++dy) {
                            mutablePos.setY(y + dy);
                            for (int dz = -4; dz <= 4; ++dz) {
                                mutablePos.setZ(z + dz);
                                if (level.getBlockState(mutablePos).getBlock() == crackedBlocks.cracked) {
                                    chance *= 0.5f;
                                    if (++count == 10) {
                                        chance = 0;
                                        break search;
                                    }
                                }
                            }
                        }
                    }
                    if (random.nextFloat() < chance) {
                        level.setBlockAndUpdate(pos, crackedBlocks.cracked.withPropertiesOf(state));
                        return;
                    }
                }
            }
        }
        MossyBlocks mossyBlocks = MossyBlocks.anyMatch(block);
        if (mossyBlocks != null) {
            if (random.nextFloat() < 0.1f) {
                Biome biome = level.getBiome(pos).value();
                if (biome.getBaseTemperature() >= 0.15f) {
                    boolean exposed = false;
                    for (Direction dir : Util.DIRECTIONS) {
                        BlockPos relativePos = pos.relative(dir);
                        if (!level.getBlockState(relativePos).isFaceSturdy(level, relativePos, dir.getOpposite())) {
                            exposed = true;
                            break;
                        }
                    }
                    if (exposed) {
                        int mossCount = 0;
                        int x = pos.getX();
                        int y = pos.getY();
                        int z = pos.getZ();
                        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
                        float chanceByNeighbours = 1.0f;
                        float chanceByWater = biome.climateSettings.downfall() * 0.1f;
                        search:
                        for (int dx = -4; dx <= 4; ++dx) {
                            mutablePos.setX(x + dx);
                            for (int dy = -4; dy <= 4; ++dy) {
                                mutablePos.setY(y + dy);
                                for (int dz = -4; dz <= 4; ++dz) {
                                    mutablePos.setZ(z + dz);
                                    if (MossyBlocks.isMoss(level.getBlockState(mutablePos).getBlock())) {
                                        chanceByNeighbours *= 0.5f;
                                        if (++mossCount == 10) {
                                            chanceByNeighbours = 0;
                                            break search;
                                        }
                                    }
                                    if (chanceByWater < 1 && level.getFluidState(mutablePos).is(FluidTags.WATER)) {
                                        chanceByWater += 0.1f;
                                    }
                                }
                            }
                        }
                        if (random.nextFloat() < chanceByNeighbours * chanceByWater) {
                            level.setBlockAndUpdate(pos, mossyBlocks.mossyBlock.withPropertiesOf(state));
//                            return;
                        }
                    }
                }
            }
        }
//        if (block == Blocks.COBBLESTONE) {
//            if (random.nextFloat() < 0.02f) {
//                if (!FallingBlock.isFree(level.getBlockState(pos.below())) && level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos) == pos.getY() + 1) {
//                    int count = 0;
//                    int x = pos.getX();
//                    int y = pos.getY();
//                    int z = pos.getZ();
//                    BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
//                    float chance = 1.0f;
//                    search:
//                    for (int dx = -4; dx <= 4; ++dx) {
//                        mutablePos.setX(x + dx);
//                        for (int dy = -4; dy <= 4; ++dy) {
//                            mutablePos.setY(y + dy);
//                            for (int dz = -4; dz <= 4; ++dz) {
//                                mutablePos.setZ(z + dz);
//                                if (level.getBlockState(mutablePos).getBlock() == Blocks.GRAVEL) {
//                                    chance *= 0.5f;
//                                    if (++count == 10) {
//                                        chance = 0;
//                                        break search;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    if (random.nextFloat() < chance) {
//                        level.setBlockAndUpdate(pos, Blocks.GRAVEL.defaultBlockState());
//                    }
//                }
//            }
//        }
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(CHUNK_LOADER_MANAGER::initialize);
        SCAFFOLDING_TIME.defaultReturnValue(-1);
        registerServerEvents();
        LOGGER.info("WolfTweaks initialized");
    }
}
