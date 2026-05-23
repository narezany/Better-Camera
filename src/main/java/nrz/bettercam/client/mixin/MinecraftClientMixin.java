package nrz.bettercam.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import nrz.bettercam.ModConfig;
import nrz.bettercam.client.MultiplayerWarningScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof JoinMultiplayerScreen && !(screen instanceof MultiplayerWarningScreen)) {
            if (!ModConfig.INSTANCE.hasAcceptedMultiplayerWarning) {
                Minecraft client = (Minecraft) (Object) this;
                ci.cancel();
                client.setScreen(new MultiplayerWarningScreen(client.screen));
            }
        }
    }
}
