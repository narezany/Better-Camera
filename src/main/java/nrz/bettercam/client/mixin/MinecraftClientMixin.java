package nrz.bettercam.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import nrz.bettercam.ModConfig;
import nrz.bettercam.client.MultiplayerWarningScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof MultiplayerScreen && !(screen instanceof MultiplayerWarningScreen)) {
            if (!ModConfig.INSTANCE.hasAcceptedMultiplayerWarning) {
                MinecraftClient client = (MinecraftClient) (Object) this;
                ci.cancel();
                client.setScreen(new MultiplayerWarningScreen(client.currentScreen));
            }
        }
    }
}
