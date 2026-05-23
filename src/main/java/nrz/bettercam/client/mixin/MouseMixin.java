package nrz.bettercam.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.option.Perspective;
import nrz.bettercam.client.CameraState;
import nrz.bettercam.client.CameraZoomState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow @Final private MinecraftClient client;
    @Shadow private double cursorDeltaX;
    @Shadow private double cursorDeltaY;

    @Inject(method = "updateMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"), cancellable = true)
    private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
        if (this.client.options.getPerspective() == Perspective.THIRD_PERSON_BACK) {
            double d = this.client.options.getMouseSensitivity().getValue() * 0.6F + 0.2F;
            double e = d * d * d;
            double f = e * 8.0;
            
            double dx = this.cursorDeltaX * f;
            double dy = this.cursorDeltaY * f;
            
            CameraState.rotateCamera(
                this.client.options.getInvertMouseX().getValue() ? -dx : dx,
                this.client.options.getInvertMouseY().getValue() ? -dy : dy
            );
            ci.cancel();
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (window == this.client.getWindow().getHandle() && this.client.getOverlay() == null && this.client.currentScreen == null) {
            boolean isThirdPerson = this.client.options.getPerspective() == Perspective.THIRD_PERSON_BACK;
            boolean isFirstPersonZoom = this.client.options.getPerspective() == Perspective.FIRST_PERSON && nrz.bettercam.ModConfig.INSTANCE.enableFirstPersonZoom;
            if (this.client.player != null && (isThirdPerson || isFirstPersonZoom)) {
                double d = this.client.options.getMouseWheelSensitivity().getValue();
                double f = (this.client.options.getDiscreteMouseScroll().getValue() ? Math.signum(vertical) : vertical) * d;
                CameraZoomState.zoom(f);
                ci.cancel();
            }
        }
    }
}
