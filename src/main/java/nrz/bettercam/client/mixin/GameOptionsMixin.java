package nrz.bettercam.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.CameraType;
import nrz.bettercam.client.CameraState;
import nrz.bettercam.client.CameraZoomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class GameOptionsMixin {
    @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
    private void onSetPerspective(CameraType perspective, CallbackInfo ci) {
        if (CameraZoomState.forceSetPerspective) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (!nrz.bettercam.ModConfig.INSTANCE.enableTransitions) {
            if (perspective == CameraType.THIRD_PERSON_BACK) {
                CameraState.initialize();
                CameraZoomState.currentDistance = CameraZoomState.targetDistance;
            } else {
                CameraState.initialized = false;
            }
            CameraZoomState.transitioningToFirstPerson = false;
            return;
        }

        if (perspective == CameraType.FIRST_PERSON) {
            if (client.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
                // Intercept the direct change and trigger a smooth transition zoom-in
                CameraZoomState.transitioningToFirstPerson = true;
                ci.cancel();
            }
        } else if (perspective == CameraType.THIRD_PERSON_BACK) {
            CameraState.initialize();
            CameraZoomState.currentDistance = 0.0f;
            if (CameraZoomState.preferredDistance < 1.0F) {
                CameraZoomState.preferredDistance = 4.0F;
            }
            CameraZoomState.targetDistance = CameraZoomState.preferredDistance;
            CameraZoomState.transitioningToFirstPerson = false;
        } else {
            CameraState.initialized = false;
            CameraZoomState.transitioningToFirstPerson = false;
        }
    }
}
