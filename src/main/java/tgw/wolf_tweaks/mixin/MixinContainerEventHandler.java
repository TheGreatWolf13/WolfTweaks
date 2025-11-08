package tgw.wolf_tweaks.mixin;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.worldselection.EditGameRulesScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerEventHandler.class)
public interface MixinContainerEventHandler extends GuiEventListener {

    @Shadow
    @Nullable GuiEventListener getFocused();

    @Inject(method = "mouseReleased", at = @At(value = "RETURN", ordinal = 0))
    default void onMouseReleased(MouseButtonEvent mouseButtonEvent, CallbackInfoReturnable<Boolean> cir) {
        GuiEventListener focused = this.getFocused();
        if (!(focused instanceof EditBox) && !(focused instanceof EditGameRulesScreen.IntegerRuleEntry)) {
            this.setFocused(null);
        }
    }

    @Shadow
    void setFocused(@Nullable GuiEventListener guiEventListener);
}
