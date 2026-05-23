package nrz.bettercam.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import nrz.bettercam.client.ConfigScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public class OptionsScreenMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        OptionsScreen screen = (OptionsScreen) (Object) this;
        ((ScreenAccessor) screen).invokeAddRenderableWidget(Button.builder(
            Component.translatable("options.better_camera.button"),
            button -> Minecraft.getInstance().setScreen(new ConfigScreen(screen))
        ).bounds(screen.width - 110, 5, 100, 20).build());
    }
}
