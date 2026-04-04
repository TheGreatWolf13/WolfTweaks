package tgw.wolf_tweaks.mixin;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import tgw.wolf_tweaks.patches.PatchOptions;

@Mixin(Options.class)
public abstract class MixinOptions implements PatchOptions {

    @Unique private final OptionInstance<@NotNull Boolean> toggleRGB = new OptionInstance<>("wolf_tweaks.gui.option.rgb", OptionInstance.noTooltip(), (_, bool) -> bool ? CommonComponents.GUI_YES : CommonComponents.GUI_NO, OptionInstance.BOOLEAN_VALUES, false, _ -> {});

    @Override
    public OptionInstance<@NotNull Boolean> toggleRGB() {
        return this.toggleRGB;
    }
}
