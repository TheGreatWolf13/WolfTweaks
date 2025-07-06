package tgw.wolf_tweaks.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.wolf_tweaks.util.Converter;

@Mixin(HorseInventoryScreen.class)
public abstract class MixinHorseInventoryScreen extends AbstractContainerScreen<HorseInventoryMenu> {

    @Shadow @Final private AbstractHorse horse;

    public MixinHorseInventoryScreen(HorseInventoryMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Override
    public void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
        super.renderLabels(gui, mouseX, mouseY);
        boolean hasChest = this.horse instanceof AbstractChestedHorse h && h.hasChest();
        double jumpValue = Math.round(Converter.jumpStrengthToJumpHeight(this.horse.getJumpPower()) * 10) / 10.0;
        double healthValue = Math.round(this.horse.getMaxHealth() * 10) / 10.0;
        double speedValue = Math.round(Converter.genericSpeedToBlockPerSec(this.horse.getAttributes().getValue(Attributes.MOVEMENT_SPEED)) * 10) / 10.0;
        int jumpColor = 0xff46_4646;
        int speedColor = 0xff46_4646;
        int hearthColor = 0xff46_4646;
        if (jumpValue > 4) {
            jumpColor = 0xff00_b400;
        }
        else if (jumpValue < 2.5f) {
            jumpColor = 0xffff_0000;
        }
        if (speedValue > 11) {
            speedColor = 0xff00_b400;
        }
        else if (speedValue < 7) {
            speedColor = 0xffff_0000;
        }
        if (healthValue > 25) {
            hearthColor = 0xff00_b400;
        }
        else if (healthValue < 20) {
            hearthColor = 0xffff_0000;
        }
        if (!hasChest) {
            gui.drawString(this.font, "(4.7-14.2)", 119, 26, 0xff46_4646, false);
            gui.drawString(this.font, "(1-5.3)", 119, 36, 0xff46_4646, false);
            gui.drawString(this.font, "(15-30)", 119, 46, 0xff46_4646, false);
            gui.drawString(this.font, "➟", 82, 26, speedColor, false);
            gui.drawString(this.font, String.valueOf(speedValue), 93, 26, speedColor, false);
            gui.drawString(this.font, "⇮", 84, 36, jumpColor, false);
            gui.drawString(this.font, String.valueOf(jumpValue), 93, 36, jumpColor, false);
            gui.drawString(this.font, "♥", 83, 46, hearthColor, false);
            gui.drawString(this.font, String.valueOf(healthValue), 93, 46, hearthColor, false);
        }
        else {
            gui.drawString(this.font, "➟ " + speedValue, 80, 6, speedColor, false);
            gui.drawString(this.font, "⇮ " + jumpValue, 115, 6, jumpColor, false);
            gui.drawString(this.font, "♥ " + healthValue, 140, 6, hearthColor, false);
        }
        if (this.horse instanceof Llama llama) {
            int strength = 3 * llama.getStrength();
            int strengthColor = 0xff46_4646;
            if (strength > 9) {
                strengthColor = 0xff00_b400;
            }
            else if (strength < 6) {
                strengthColor = 0xffff_0000;
            }
            if (!hasChest) {
                gui.drawString(this.font, "▦", 83, 56, strengthColor, false);
                gui.drawString(this.font, String.valueOf(strength), 93, 56, strengthColor, false);
            }
        }
    }
}
