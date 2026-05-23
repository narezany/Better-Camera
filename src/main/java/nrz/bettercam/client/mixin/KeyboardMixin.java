package nrz.bettercam.client.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import nrz.bettercam.client.CameraState;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void onKey(long window, int action, net.minecraft.client.input.KeyEvent event, CallbackInfo ci) {
        if (window == this.minecraft.getWindow().handle() && this.minecraft.screen == null) {
            int key = event.key();
            if (key == GLFW.GLFW_KEY_LEFT_CONTROL && action == GLFW.GLFW_PRESS) {
                if (nrz.bettercam.ModConfig.INSTANCE.enableCtrlLock && this.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
                    CameraState.shiftLock = !CameraState.shiftLock;
                }
            }
            if (key == GLFW.GLFW_KEY_K && action == GLFW.GLFW_PRESS) {
                this.minecraft.setScreen(new nrz.bettercam.client.ConfigScreen(this.minecraft.screen));
            }
        }
    }
}
