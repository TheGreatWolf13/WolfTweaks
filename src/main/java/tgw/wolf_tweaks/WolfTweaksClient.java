package tgw.wolf_tweaks;

import com.google.gson.Gson;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceIntPair;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;
import tgw.wolf_tweaks.patches.PatchOptions;
import tgw.wolf_tweaks.util.collection.lists.OArrayList;
import tgw.wolf_tweaks.util.collection.lists.OList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public final class WolfTweaksClient implements ClientModInitializer {

    private static final Component INSTANT_MINE = Component.translatable("wolf_tweaks.mining_time.instant_mine").withStyle(ChatFormatting.BLUE);
    private static final Reference2IntMap<PacketType<?>> PACKETS_BY_TYPE = new Reference2IntOpenHashMap<>();
    private static final Gson GSON = new Gson();
    private static final GSIState GSI_STATE = new GSIState();
    private static final ResourceLocation RANDOM_PLACEMENT_TEXTURE = ResourceLocation.fromNamespaceAndPath("wolf_tweaks", "textures/gui/random_block_placement.png");
    private static KeyMapping buildingReplace;
    public static boolean placedBlock;
    private static KeyMapping randomPlaceKey;
    private static boolean randomPlacementMode;
    private static int tickCount;

    public static void appendMiningSpeed(Block block, Consumer<Component> consumer) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        float hardness = block.defaultDestroyTime();
        if (hardness == 0) {
            consumer.accept(INSTANT_MINE);
        }
        else {
            ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            Tool tool = stack.get(DataComponents.TOOL);
            BlockState state = block.defaultBlockState();
            boolean canHarvest = !state.requiresCorrectToolForDrops() || tool != null && tool.isCorrectForDrops(state);
            double miningSpeed = player.getDestroySpeed(state);
            double damage = miningSpeed / hardness;
            if (canHarvest) {
                damage /= 30;
            }
            else {
                damage /= 100;
            }
            if (damage > 1) {
                consumer.accept(INSTANT_MINE);
            }
            else {
                int ticks = Mth.ceil(1 / damage);
                double seconds = ticks / 20.0;
                consumer.accept(Component.translatable("wolf_tweaks.mining_time.time", String.format("%.2f", seconds)).withStyle(ChatFormatting.BLUE));
            }
        }
    }

    public static void clearPacketRecord() {
        PACKETS_BY_TYPE.clear();
    }

    private static void randomizeHotbarSlot(LocalPlayer player) {
        int blockCount = 0;
        int validSlots = 0;
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).getItem() instanceof BlockItem) {
                ++blockCount;
                validSlots |= 1 << i;
            }
        }
        if (blockCount <= 1) {
            return;
        }
        RandomSource random = player.getRandom();
        int randomSlot = random.nextInt(9);
        while ((validSlots >> randomSlot & 1) == 0) {
            randomSlot = random.nextInt(9);
        }
        player.getInventory().setSelectedSlot(randomSlot);
    }

    public static void recordPacket(Packet<?> packet) {
        PACKETS_BY_TYPE.mergeInt(packet.type(), 1, Integer::sum);
    }

    private static void renderTexture(GuiGraphics gui, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) {
            return;
        }
        mc.getTextureManager().getTexture(RANDOM_PLACEMENT_TEXTURE);
        int x = screenWidth / 2;
        int y = screenHeight / 2 - 4;
        Matrix3x2fStack matrixStack = gui.pose();
        matrixStack.pushMatrix();
        matrixStack.translate(0, -15);
        gui.drawCenteredString(mc.font, Component.translatable("wolf_tweaks.gui.random_block_placement"), x, y, 0xFFFF_FFFF);
        matrixStack.popMatrix();
    }

    private static void sendChromaState(Minecraft mc) {
        if (((PatchOptions) mc.options).toggleRGB().get()) {
            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                HttpPost request = new HttpPost("http://localhost:9088");
                request.addHeader("Content-Type", "application/json");
                request.setEntity(new StringEntity(GSON.toJson(GSI_STATE.update(mc))));
                httpClient.execute(request);
            }
            catch (Exception ignore) {
            }
        }
    }

    @Override
    public void onInitializeClient() {
        buildingReplace = KeyBindingHelper.registerKeyBinding(new KeyMapping("wolf_tweaks.key.replace", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, "key.categories.creative"));
        randomPlaceKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("wolf_tweaks.key.random_block_placement_toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.gameplay"));
        ClientSendMessageEvents.COMMAND.register(cmd -> {
            if ("packetinfo".equals(cmd)) {
                if (PACKETS_BY_TYPE.isEmpty()) {
                    //noinspection DataFlowIssue
                    Minecraft.getInstance().player.displayClientMessage(Component.literal("Total: 0"), false);
                    return;
                }
                int total = 0;
                OList<ReferenceIntPair<PacketType<?>>> ordered = new OArrayList<>();
                for (Reference2IntMap.Entry<PacketType<?>> entry : PACKETS_BY_TYPE.reference2IntEntrySet()) {
                    total += entry.getIntValue();
                    //noinspection ObjectAllocationInLoop
                    ordered.add(ReferenceIntPair.of(entry.getKey(), entry.getIntValue()));
                }
                ordered.sort((a, b) -> Integer.compare(b.rightInt(), a.rightInt()));
                //noinspection DataFlowIssue
                Minecraft.getInstance().player.displayClientMessage(Component.literal("Total: " + total), false);
                Minecraft.getInstance().player.displayClientMessage(Component.literal(ordered.stream().map(pair -> pair.rightInt() + ": " + pair.left()).collect(Collectors.joining("\n"))), false);
                return;
            }
            if ("exportmap".equals(cmd)) {
                Minecraft mc = Minecraft.getInstance();
                assert mc.player != null;
                ItemStack stack = mc.player.getItemInHand(InteractionHand.MAIN_HAND);
                if (stack.getItem() instanceof MapItem) {
                    MapId mapId = stack.get(DataComponents.MAP_ID);
                    if (mapId == null) {
                        return;
                    }
                    assert mc.level != null;
                    MapItemSavedData mapData = mc.level.getMapData(mapId);
                    if (mapData == null) {
                        return;
                    }
                    File folder = new File(mc.gameDirectory, "exported_maps");
                    if (!folder.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        folder.mkdir();
                    }
                    File file = new File(folder, mapId.id() + ".png");
                    if (!file.exists()) {
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            file.createNewFile();
                        }
                        catch (IOException e) {
                            mc.player.displayClientMessage(Component.literal("Could not create new file to export map. See log for details."), false);
                            WolfTweaks.LOGGER.error("Could not save exported map: ", e);
                            return;
                        }
                    }
                    byte[] colors = mapData.colors;
                    BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
                    for (int i = 0; i < 128; ++i) {
                        for (int j = 0; j < 128; ++j) {
                            image.setRGB(i, j, MapColor.getColorFromPackedId(colors[i + j * 128]));
                        }
                    }
                    try {
                        ImageIO.write(image, "png", file);
                    }
                    catch (IOException e) {
                        mc.player.displayClientMessage(Component.literal("Could not save exported map. See log for details."), false);
                        WolfTweaks.LOGGER.error("Could not save exported map: ", e);
                    }
                }
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            ++tickCount;
            if ((tickCount & 7) == 0) {
                sendChromaState(mc);
            }
            ClientLevel level = mc.level;
            if (level == null) {
                return;
            }
            LocalPlayer player = mc.player;
            if (player == null) {
                return;
            }
            //noinspection VariableNotUsedInsideIf
            if (mc.screen != null) {
                return;
            }
            while (randomPlaceKey.consumeClick()) {
                randomPlacementMode = !randomPlacementMode;
            }
            if (randomPlacementMode && placedBlock) {
                randomizeHotbarSlot(player);
            }
            placedBlock = false;
            while (buildingReplace.consumeClick()) {
                if (player.isCreative()) {
                    HitResult hitResult = mc.hitResult;
                    if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                        ItemStack stack = player.getMainHandItem();
                        if (stack.getItem() instanceof BlockItem blockItem) {
                            Block block = blockItem.getBlock();
                            BlockHitResult blockHit = (BlockHitResult) hitResult;
                            //noinspection ObjectAllocationInLoop
                            BlockState state = block.getStateForPlacement(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, blockHit));
                            if (state != null) {
                                if (block instanceof SlabBlock) {
                                    if (blockHit.getDirection() == Direction.UP) {
                                        state = state.setValue(SlabBlock.TYPE, SlabType.TOP);
                                    }
                                }
                                BlockState finalState = state;
                                BlockPos pos = blockHit.getBlockPos();
                                //noinspection ObjectAllocationInLoop
                                String properties = state.getProperties().stream().map(p -> p.value(finalState).toString()).collect(Collectors.joining(","));
                                //noinspection ObjectAllocationInLoop
                                player.connection.sendCommand("setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + state.getBlockHolder().getRegisteredName() + "[" + properties + "]");
                            }
                        }
                    }
                }
            }
        });
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (randomPlacementMode) {
                Window window = Minecraft.getInstance().getWindow();
                int screenWidth = window.getGuiScaledWidth();
                int screenHeight = window.getGuiScaledHeight();
                renderTexture(drawContext, screenWidth, screenHeight);
            }
        });
    }
}
