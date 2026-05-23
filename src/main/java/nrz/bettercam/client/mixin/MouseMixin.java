package nrz.bettercam.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.CameraType;
import nrz.bettercam.client.CameraState;
import nrz.bettercam.client.CameraZoomState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private double accumulatedDX;
    @Shadow private double accumulatedDY;

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void onUpdateMouse(CallbackInfo ci) {
        if (this.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            if (this.minecraft.player != null) {
                double d = this.minecraft.options.sensitivity().get() * 0.6F + 0.2F;
                double e = d * d * d;
                double f = e * 8.0;
                
                double dx = this.accumulatedDX * f;
                double dy = this.accumulatedDY * f;
                
                boolean invertY = this.minecraft.options.invertMouseY().get();
                
                CameraState.rotateCamera(dx, invertY ? -dy : dy);
            }
            this.accumulatedDX = 0.0;
            this.accumulatedDY = 0.0;
            ci.cancel();
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == this.minecraft.getWindow().handle() && this.minecraft.getOverlay() == null && this.minecraft.screen == null) {
            boolean isThirdPerson = this.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK;
            boolean isFirstPersonZoom = this.minecraft.options.getCameraType() == CameraType.FIRST_PERSON && nrz.bettercam.ModConfig.INSTANCE.enableFirstPersonZoom;
            if (this.minecraft.player != null && (isThirdPerson || isFirstPersonZoom)) {
                double d = this.minecraft.options.mouseWheelSensitivity().get();
                double f = (this.minecraft.options.discreteMouseScroll().get() ? Math.signum(vertical) : vertical) * d;
                CameraZoomState.zoom(f);
                ci.cancel();
            }
        }
    }
}
