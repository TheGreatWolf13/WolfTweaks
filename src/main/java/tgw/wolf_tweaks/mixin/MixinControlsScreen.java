package tgw.wolf_tweaks.mixin;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tgw.wolf_tweaks.patches.PatchOptions;

@Mixin(ControlsScreen.class)
public abstract class MixinControlsScreen extends OptionsSubScreen {

    public MixinControlsScreen(Screen screen, Options options, Component component) {
        super(screen, options, component);
    }

    @ModifyConstant(method = "options", constant = @Constant(intValue = 4))
    private static int options_int_4(int constant) {
        return 5;
    }

    @Inject(method = "options", at = @At("RETURN"))
    private static void options_return(Options options, CallbackInfoReturnable<OptionInstance<?>[]> cir) {
        OptionInstance<?>[] returnValue = cir.getReturnValue();
        returnValue[4] = ((PatchOptions) options).toggleRGB();
    }
}
