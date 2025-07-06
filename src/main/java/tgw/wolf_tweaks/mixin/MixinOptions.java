package tgw.wolf_tweaks.mixin;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.CommonComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tgw.wolf_tweaks.patches.PatchOptions;

@Mixin(Options.class)
public abstract class MixinOptions implements PatchOptions {

    @Unique private final OptionInstance<Boolean> toggleRGB = new OptionInstance<>("wolf_tweaks.gui.option.rgb", OptionInstance.noTooltip(), (component, boolean_) -> boolean_ ? CommonComponents.GUI_YES : CommonComponents.GUI_NO, OptionInstance.BOOLEAN_VALUES, false, boolean_ -> {});

    @Override
    public OptionInstance<Boolean> toggleRGB() {
        return this.toggleRGB;
    }
}
