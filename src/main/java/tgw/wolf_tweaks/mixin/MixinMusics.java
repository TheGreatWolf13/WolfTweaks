package tgw.wolf_tweaks.mixin;

import net.minecraft.sounds.Musics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Musics.class)
public abstract class MixinMusics {

    /**
     * Creative Music Min Delay 10min -> 30s
     */
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 12_000))
    private static int _clinit_12000(int oldValue) {
        return 30 * 20;
    }

    /**
     * Creative Music Max Delay 20min -> 2min30s
     * End Music Max Delay 20min -> 2min30s
     */
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 24_000))
    private static int _clinit_24000(int oldValue) {
        return 2 * 60 * 20 + 30 * 20;
    }

    /**
     * Menu Music Min Delay 30s -> 5s
     */
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 600))
    private static int _clinit_600(int oldValue) {
        return 5 * 20;
    }

    /**
     * End Music Min Delay 5min -> 30s
     */
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 6_000))
    private static int _clinit_6000(int oldValue) {
        return 30 * 20;
    }

    /**
     * Game Music Min Delay 10min -> 30s
     * Underwater Music Min Delay 10min -> 30s
     */
    @ModifyConstant(method = "createGameMusic", constant = @Constant(intValue = 12_000))
    private static int createGameMusic_12000(int oldValue) {
        return 30 * 20;
    }

    /**
     * Game Music Max Delay 20min -> 2min30s
     * Underwater Music Max Delay 20min -> 2min30s
     */
    @ModifyConstant(method = "createGameMusic", constant = @Constant(intValue = 24_000))
    private static int createGameMusic_24000(int oldValue) {
        return 2 * 60 * 20 + 30 * 20;
    }
}
