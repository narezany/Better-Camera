package nrz.bettercam.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import nrz.bettercam.client.CameraState;
import nrz.bettercam.client.CameraZoomState;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow private int matrixPropertiesDirty;
    @Shadow private boolean detached;
    @Shadow protected abstract void move(float surge, float heave, float sway);
    
    @Shadow private float xRot;
    @Shadow private float yRot;
    @Shadow @Final private Quaternionf rotation;
    @Shadow @Final private Vector3f left;
    @Shadow @Final private Vector3f up;
    @Shadow @Final private Vector3f forwards;
    
    @Shadow @Final private static Vector3f LEFT;
    @Shadow @Final private static Vector3f UP;
    @Shadow @Final private static Vector3f FORWARDS;

    @Inject(method = "alignWithEntity", at = @At("HEAD"))
    private void onUpdateHead(float tickProgress, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            if (!CameraState.initialized) {
                CameraState.initialize();
                // Smoothly zoom out from the character's head on transition
                CameraZoomState.currentDistance = 0.0f;
            }
            CameraZoomState.update();
            CameraState.updateTransition();
        }
    }

    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    private void onSetRotation(float yaw, float pitch, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (this.detached && client.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            float customYaw = CameraState.cameraYaw;
            float customPitch = CameraState.cameraPitch;
            
            this.xRot = customPitch;
            this.yRot = customYaw;
            this.rotation.rotationYXZ((float) Math.PI - customYaw * (float) (Math.PI / 180.0), -customPitch * (float) (Math.PI / 180.0), 0.0F);
            LEFT.rotate(this.rotation, this.left);
            UP.rotate(this.rotation, this.up);
            FORWARDS.rotate(this.rotation, this.forwards);
            this.matrixPropertiesDirty |= 3;
            
            ci.cancel();
        }
    }

    @ModifyArg(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getMaxZoom(F)F"), index = 0)
    private float modifyCameraDistance(float distance) {
        Minecraft client = Minecraft.getInstance();
        if (client.options.getCameraType() == CameraType.FIRST_PERSON) {
            return 0.0F;
        }
        if (this.detached && client.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            return CameraZoomState.currentDistance;
        }
        return distance;
    }

    @Inject(method = "alignWithEntity", at = @At("TAIL"))
    private void onUpdateTail(float tickProgress, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.options.getCameraType() == CameraType.FIRST_PERSON) {
            this.detached = false;
        }
        if (client.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            if (CameraState.currentShiftLockAlpha > 0.001f) {
                // Smoothly apply camera offset up and to the right
                float heave = 0.4f * CameraState.currentShiftLockAlpha;
                float sway = 0.8f * CameraState.currentShiftLockAlpha;
                this.move(0.0f, heave, sway);
            }
        }
    }
}
