package tgw.wolf_tweaks.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Screen.class)
public abstract class MixinScreen extends AbstractContainerEventHandler implements Renderable {

    @Shadow protected @Nullable Minecraft minecraft;

    @Redirect(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderBlurredBackground(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public void onRenderBackground_renderBlurredBackground(Screen instance, GuiGraphics guiGraphics) {
        //Do nothing
    }

    @Redirect(method = "renderBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderMenuBackground(Lnet/minecraft/client/gui/GuiGraphics;)V"))
    public void onRenderBackground_renderMenuBackground(Screen instance, GuiGraphics guiGraphics) {
        //noinspection VariableNotUsedInsideIf,DataFlowIssue
        if (this.minecraft.level != null) {
            this.renderTransparentBackground(guiGraphics);
        }
    }

    @Shadow
    public abstract void renderTransparentBackground(GuiGraphics guiGraphics);
}
