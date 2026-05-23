package nrz.bettercam.client.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import nrz.bettercam.client.CameraState;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"))
    private void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (window == this.client.getWindow().getHandle() && this.client.currentScreen == null) {
            if (key == GLFW.GLFW_KEY_LEFT_CONTROL && action == GLFW.GLFW_PRESS) {
                if (nrz.bettercam.ModConfig.INSTANCE.enableCtrlLock && this.client.options.getPerspective() == Perspective.THIRD_PERSON_BACK) {
                    CameraState.shiftLock = !CameraState.shiftLock;
                }
            }
            if (key == GLFW.GLFW_KEY_K && action == GLFW.GLFW_PRESS) {
                this.client.setScreen(new nrz.bettercam.client.ConfigScreen(this.client.currentScreen));
            }
        }
    }
}
