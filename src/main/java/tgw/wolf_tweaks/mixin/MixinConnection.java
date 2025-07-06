package tgw.wolf_tweaks.mixin;

import io.netty.channel.SimpleChannelInboundHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.wolf_tweaks.WolfTweaksClient;

@Mixin(Connection.class)
public abstract class MixinConnection extends SimpleChannelInboundHandler<Packet<?>> {

    @Shadow @Final private PacketFlow receiving;

    @Redirect(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketListener;shouldHandleMessage(Lnet/minecraft/network/protocol/Packet;)Z"))
    private boolean channelRead0_shouldHandleMessage(PacketListener listener, Packet<?> packet) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && this.receiving == PacketFlow.CLIENTBOUND) {
            WolfTweaksClient.recordPacket(packet);
        }
        return listener.shouldHandleMessage(packet);
    }

    @Inject(method = "tickSecond", at = @At("TAIL"))
    private void tickSecond_tail(CallbackInfo ci) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && this.receiving == PacketFlow.CLIENTBOUND) {
            WolfTweaksClient.clearPacketRecord();
        }
    }
}
