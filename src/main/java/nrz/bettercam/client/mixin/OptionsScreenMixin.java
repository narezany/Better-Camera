package nrz.bettercam.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
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
        ((ScreenAccessor) screen).invokeAddDrawableChild(ButtonWidget.builder(
            Text.translatable("options.better_camera.button"),
            button -> MinecraftClient.getInstance().setScreen(new ConfigScreen(screen))
        ).dimensions(screen.width - 110, 5, 100, 20).build());
    }
}
