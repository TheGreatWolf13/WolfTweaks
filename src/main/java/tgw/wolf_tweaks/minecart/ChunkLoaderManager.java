package tgw.wolf_tweaks.minecart;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import tgw.wolf_tweaks.WolfTweaks;
import tgw.wolf_tweaks.util.collection.lists.OArrayList;
import tgw.wolf_tweaks.util.collection.lists.OList;
import tgw.wolf_tweaks.util.collection.maps.L2OHashMap;
import tgw.wolf_tweaks.util.collection.maps.L2OMap;
import tgw.wolf_tweaks.util.collection.maps.O2OHashMap;
import tgw.wolf_tweaks.util.collection.maps.O2OMap;
import tgw.wolf_tweaks.util.collection.sets.LHashSet;
import tgw.wolf_tweaks.util.collection.sets.LSet;
import tgw.wolf_tweaks.util.collection.sets.OHashSet;
import tgw.wolf_tweaks.util.collection.sets.OSet;

import java.util.UUID;

public class ChunkLoaderManager {
    public O2OMap<ResourceKey<Level>, L2OMap<OList<UUID>>> forceLoadedChunks = new O2OHashMap<>();
    private boolean initialized;
    private long lastTick;
    private final OSet<Entity> pendingRegistrations = new OHashSet<>();
    MinecraftServer server;

    private static void buildChunkSquare(long chunkPos, LSet toAdd) {
        int x = ChunkPos.getX(chunkPos);
        int z = ChunkPos.getZ(chunkPos);
        for (int dz = 0; dz < 3; ++dz) {
            for (int dx = 0; dx < 3; ++dx) {
                toAdd.add(ChunkPos.asLong(x + dx - 1, z + dz - 1));
            }
        }
    }

    private LSet calculateLoadedChunks(ServerLevel level) {
        LSet chunks = new LHashSet();
        L2OMap<OList<UUID>> levelChunks = this.forceLoadedChunks.get(level.dimension());
        if (levelChunks != null) {
            for (long it = levelChunks.beginIteration(); levelChunks.hasNextIteration(it); it = levelChunks.nextEntry(it)) {
                buildChunkSquare(levelChunks.getIterationKey(it), chunks);
            }
        }
        return chunks;
    }

    private void handlePendingRegistrations() {
        WolfTweaks.LOGGER.info("Handling pending registrations");
        OSet<Entity> pendingRegistrations = this.pendingRegistrations;
        for (long it = pendingRegistrations.beginIteration(); pendingRegistrations.hasNextIteration(it); it = pendingRegistrations.nextEntry(it)) {
            this.registerChunkLoader(pendingRegistrations.getIteration(it));
        }
        pendingRegistrations.clear();
    }

    public void initialize(MinecraftServer server) {
        this.server = server;
        this.setupTimer();
        this.initialized = true;
        this.handlePendingRegistrations();
    }

    public void registerChunkLoader(Entity entity) {
        if (!this.initialized) {
            this.pendingRegistrations.add(entity);
            return;
        }
        ChunkPos chunkPos = entity.chunkPosition();
        this.removeChunkLoader(entity);
        WolfTweaks.LOGGER.info("Adding {} to {}", entity, entity.level().dimension().location());
        ResourceKey<Level> worldRegistryKey = entity.level().dimension();
        L2OMap<OList<UUID>> levelChunks = this.forceLoadedChunks.get(worldRegistryKey);
        OList<UUID> list;
        if (levelChunks == null) {
            levelChunks = new L2OHashMap<>();
            this.forceLoadedChunks.put(worldRegistryKey, levelChunks);
            list = new OArrayList<>();
            levelChunks.put(chunkPos.toLong(), list);
        }
        else {
            long pos = chunkPos.toLong();
            list = levelChunks.get(pos);
            if (list == null) {
                list = new OArrayList<>();
                levelChunks.put(pos, list);
            }
        }
        list.add(entity.getUUID());
    }

    public void removeChunkLoader(Entity entity) {
        WolfTweaks.LOGGER.info("Removing {} from {}", entity, entity.level().dimension().location());
        UUID uuid = entity.getUUID();
        ResourceKey<Level> worldRegistryKey = entity.level().dimension();
        L2OMap<OList<UUID>> levelChunks = this.forceLoadedChunks.get(worldRegistryKey);
        WolfTweaks.LOGGER.info("levelChunks {}", levelChunks);
        if (levelChunks == null) {
            return;
        }
        for (long it = levelChunks.beginIteration(); levelChunks.hasNextIteration(it); it = levelChunks.nextEntry(it)) {
            OList<UUID> uuids = levelChunks.getIterationValue(it);
            boolean didRemove = uuids.remove(uuid);
            if (didRemove && uuids.isEmpty()) {
                it = levelChunks.removeIteration(it);
            }
        }
    }

    private void setupTimer() {
        ServerTickEvents.END_WORLD_TICK.register(level -> {
            long time = level.getGameTime();
            if (time != this.lastTick && time % 20 == 0) {
                this.lastTick = time;
                this.updateWorlds();
            }
        });
    }

    private void updateChunkLoaders(ServerLevel level) {
        LSet currentChunks = this.calculateLoadedChunks(level);
        final LongSet forcedChunks = level.getForcedChunks();
        forcedChunks.forEach(pos -> {
            if (!currentChunks.contains(pos)) {
                // Unload chunk
                level.setChunkForced(ChunkPos.getX(pos), ChunkPos.getZ(pos), false);
            }
        });
        for (long it = currentChunks.beginIteration(); currentChunks.hasNextIteration(it); it = currentChunks.nextEntry(it)) {
            long chunkPos = currentChunks.getIteration(it);
            if (!forcedChunks.contains(chunkPos)) {
                // Load chunk
                level.setChunkForced(ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), true);
            }
        }
    }

    private void updateWorlds() {
        O2OMap<ResourceKey<Level>, L2OMap<OList<UUID>>> chunks = this.forceLoadedChunks;
        for (long it = chunks.beginIteration(); chunks.hasNextIteration(it); it = chunks.nextEntry(it)) {
            ServerLevel level = this.server.getLevel(chunks.getIterationKey(it));
            if (level != null) {
                this.updateChunkLoaders(level);
            }
        }
    }
}
