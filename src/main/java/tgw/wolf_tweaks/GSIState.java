package tgw.wolf_tweaks;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class GSIState {

    private final GameNode game = new GameNode();
    private final PlayerNode player = new PlayerNode();
    private final ProviderNode provider = new ProviderNode();
    private final WorldNode world = new WorldNode();

    public GSIState update(Minecraft mc) {
        this.world.update(mc);
        this.player.update(mc);
        this.game.update(mc);
        return this;
    }

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private static class GameNode {
        private boolean chatGuiOpen;
        private boolean controlsGuiOpen;
        private AuroraKeyBinding @Nullable [] keys;

        private void update(Minecraft mc) {
            Screen screen = mc.screen;
            this.keys = null;
            if (screen instanceof KeyBindsScreen) {
                this.controlsGuiOpen = true;
                this.chatGuiOpen = false;
                KeyMapping[] keys = mc.options.keyMappings;
                List<AuroraKeyBinding> tempList = new ArrayList<>();
                for (KeyMapping key : keys) {
                    if (!key.getName().contains("unknown") && key.getName().contains("keyboard")) {
                        String context = "key.categories.inventory".equals(key.getCategory()) ? "GUI" : "UNIVERSAL";
                        //noinspection ObjectAllocationInLoop
                        tempList.add(new AuroraKeyBinding(AuroraKeyBinding.ToAuroraKeyCode(key.getName()), null, context));
                    }
                }
                this.keys = new AuroraKeyBinding[tempList.size()];
                this.keys = tempList.toArray(this.keys);
            }
            else if (screen instanceof ChatScreen) {
                this.chatGuiOpen = true;
                this.controlsGuiOpen = false;
            }
            else {
                this.controlsGuiOpen = false;
                this.chatGuiOpen = false;
            }
        }
    }

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private static class PlayerNode {
        private static final String[] EFFECT_NAMES = new String[28];
        private static final Holder<MobEffect>[] EFFECTS = new Holder[28];

        static {
            EFFECT_NAMES[0] = "moveSpeed";
            EFFECT_NAMES[1] = "moveSlowdown";
            EFFECT_NAMES[2] = "haste";
            EFFECT_NAMES[3] = "miningFatigue";
            EFFECT_NAMES[4] = "strength";
            EFFECT_NAMES[5] = "jumpBoost";
            EFFECT_NAMES[6] = "confusion";
            EFFECT_NAMES[7] = "regeneration";
            EFFECT_NAMES[8] = "resistance";
            EFFECT_NAMES[9] = "fireResistance";
            EFFECT_NAMES[10] = "waterBreathing";
            EFFECT_NAMES[11] = "invisibility";
            EFFECT_NAMES[12] = "blindness";
            EFFECT_NAMES[13] = "nightVision";
            EFFECT_NAMES[14] = "hunger";
            EFFECT_NAMES[15] = "weakness";
            EFFECT_NAMES[16] = "poison";
            EFFECT_NAMES[17] = "wither";
            EFFECT_NAMES[18] = "absorption";
            EFFECT_NAMES[19] = "glowing";
            EFFECT_NAMES[20] = "levitation";
            EFFECT_NAMES[21] = "luck";
            EFFECT_NAMES[22] = "badLuck";
            EFFECT_NAMES[23] = "slowFalling";
            EFFECT_NAMES[24] = "conduitPower";
            EFFECT_NAMES[25] = "dolphinsGrace";
            EFFECT_NAMES[26] = "bad_omen";
            EFFECT_NAMES[27] = "villageHero";
            EFFECTS[0] = MobEffects.SPEED;
            EFFECTS[1] = MobEffects.SLOWNESS;
            EFFECTS[2] = MobEffects.HASTE;
            EFFECTS[3] = MobEffects.MINING_FATIGUE;
            EFFECTS[4] = MobEffects.STRENGTH;
            EFFECTS[5] = MobEffects.JUMP_BOOST;
            EFFECTS[6] = MobEffects.NAUSEA;
            EFFECTS[7] = MobEffects.REGENERATION;
            EFFECTS[8] = MobEffects.RESISTANCE;
            EFFECTS[9] = MobEffects.FIRE_RESISTANCE;
            EFFECTS[10] = MobEffects.WATER_BREATHING;
            EFFECTS[11] = MobEffects.INVISIBILITY;
            EFFECTS[12] = MobEffects.BLINDNESS;
            EFFECTS[13] = MobEffects.NIGHT_VISION;
            EFFECTS[14] = MobEffects.HUNGER;
            EFFECTS[15] = MobEffects.WEAKNESS;
            EFFECTS[16] = MobEffects.POISON;
            EFFECTS[17] = MobEffects.WITHER;
            EFFECTS[18] = MobEffects.ABSORPTION;
            EFFECTS[19] = MobEffects.GLOWING;
            EFFECTS[20] = MobEffects.LEVITATION;
            EFFECTS[21] = MobEffects.LUCK;
            EFFECTS[22] = MobEffects.UNLUCK;
            EFFECTS[23] = MobEffects.SLOW_FALLING;
            EFFECTS[24] = MobEffects.CONDUIT_POWER;
            EFFECTS[25] = MobEffects.DOLPHINS_GRACE;
            EFFECTS[26] = MobEffects.BAD_OMEN;
            EFFECTS[27] = MobEffects.HERO_OF_THE_VILLAGE;
        }

        private float absorption;
        private int armor;
        private float experience;
        private int experienceLevel;
        private int foodLevel;
        private float health;
        private boolean inGame;
        private boolean isBurning;
        private boolean isDead;
        private boolean isInWater;
        private boolean isRidingHorse;
        private boolean isSneaking;
        private float maxHealth;
        private final Map<String, Boolean> playerEffects = new HashMap<>();
        private float saturationLevel;

        private void update(Minecraft mc) {
            LocalPlayer player = mc.player;
            if (player == null) {
                this.inGame = false;
                return;
            }
            this.inGame = true;
            this.health = player.getHealth();
            this.maxHealth = player.getMaxHealth();
            this.absorption = player.getAbsorptionAmount();
            this.isDead = !player.isAlive();
            this.armor = player.getArmorValue();
            this.experienceLevel = player.experienceLevel;
            this.experience = player.experienceProgress;
            FoodData foodData = player.getFoodData();
            this.foodLevel = foodData.getFoodLevel();
            this.saturationLevel = foodData.getSaturationLevel();
            this.isSneaking = player.isCrouching();
            this.isRidingHorse = player.isPassenger();
            this.isBurning = player.isOnFire();
            this.isInWater = player.isInWater();
            for (int i = 0; i < 28; ++i) {
                this.playerEffects.put(EFFECT_NAMES[i], player.getEffect(EFFECTS[i]) != null);
            }
        }
    }

    @SuppressWarnings({"FieldMayBeStatic", "unused"})
    private static class ProviderNode {
        private final int appid = -1;
        private final String name = "minecraft";
    }

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private static class WorldNode {
        private int dimensionID;
        private boolean isDayTime;
        private boolean isRaining;
        private float rainStrength;
        private long worldTime;

        private void update(Minecraft mc) {
            ClientLevel level = mc.level;
            if (level == null) {
                return;

            }
            this.worldTime = level.getDayTime();
            this.isDayTime = level.isBrightOutside();
            this.rainStrength = level.getRainLevel(1);
            this.isRaining = level.isRaining();
            ResourceKey<Level> dimension = level.dimension();
            if (dimension == Level.NETHER) {
                this.dimensionID = -1;
            }
            else if (dimension == Level.END) {
                this.dimensionID = 1;
            }
            else {
                this.dimensionID = 0;
            }
        }
    }
}

