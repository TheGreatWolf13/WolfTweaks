package tgw.wolf_tweaks.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Screen.class)
public abstract class MixinScreen extends AbstractContainerEventHandler implements Renderable {

    @Shadow @Final protected Minecraft minecraft;

    @Shadow
    public abstract void extractTransparentBackground(GuiGraphicsExtractor graphics);

    @Redirect(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;extractBlurredBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    public void onExtractBackground_renderBlurredBackground(Screen instance, GuiGraphicsExtractor graphics) {
        //Do nothing
    }

    @Redirect(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;extractMenuBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
    public void onRenderBackground_renderMenuBackground(Screen instance, GuiGraphicsExtractor graphics) {
        //noinspection VariableNotUsedInsideIf
        if (this.minecraft.level != null) {
            this.extractTransparentBackground(graphics);
        }
    }
}
