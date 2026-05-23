package nrz.bettercam.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
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
    @Shadow private boolean thirdPerson;
    @Shadow protected abstract void moveBy(double surge, double heave, double sway);
    
    @Shadow private float pitch;
    @Shadow private float yaw;
    @Shadow @Final private Quaternionf rotation;
    @Shadow @Final private Vector3f horizontalPlane;
    @Shadow @Final private Vector3f verticalPlane;
    @Shadow @Final private Vector3f diagonalPlane;

    @Inject(method = "update", at = @At("HEAD"))
    private void onUpdateHead(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
        if (thirdPerson && !inverseView) { // THIRD_PERSON_BACK
            if (!CameraState.initialized) {
                CameraState.initialize();
                // Smoothly zoom out from the character's head on transition
                CameraZoomState.currentDistance = 0.0f;
            }
            CameraZoomState.update();
            CameraState.updateTransition();
            
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.options.getPerspective() == Perspective.FIRST_PERSON) {
                this.thirdPerson = false;
            }
        }
    }

    @Inject(method = "setRotation", at = @At("HEAD"), cancellable = true)
    private void onSetRotation(float yaw, float pitch, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (this.thirdPerson && client.options.getPerspective() == Perspective.THIRD_PERSON_BACK) {
            float customYaw = CameraState.cameraYaw;
            float customPitch = CameraState.cameraPitch;
            
            this.pitch = customPitch;
            this.yaw = customYaw;
            this.rotation.rotationYXZ((float) Math.PI - customYaw * (float) (Math.PI / 180.0), -customPitch * (float) (Math.PI / 180.0), 0.0F);
            this.horizontalPlane.set(0.0F, 0.0F, -1.0F).rotate(this.rotation);
            this.verticalPlane.set(0.0F, 1.0F, 0.0F).rotate(this.rotation);
            this.diagonalPlane.set(1.0F, 0.0F, 0.0F).rotate(this.rotation);
            
            ci.cancel();
        }
    }

    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;clipToSpace(D)D"), index = 0)
    private double modifyCameraDistance(double distance) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.getPerspective() == Perspective.FIRST_PERSON) {
            return 0.0;
        }
        if (this.thirdPerson && client.options.getPerspective() == Perspective.THIRD_PERSON_BACK) {
            return CameraZoomState.currentDistance;
        }
        return distance;
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void onUpdateTail(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.getPerspective() == Perspective.FIRST_PERSON) {
            this.thirdPerson = false;
        }
        if (thirdPerson && !inverseView && client.options.getPerspective() == Perspective.THIRD_PERSON_BACK) {
            if (CameraState.currentShiftLockAlpha > 0.001f) {
                // Smoothly apply camera offset up and to the right
                float heave = 0.4f * CameraState.currentShiftLockAlpha;
                float sway = 0.8f * CameraState.currentShiftLockAlpha;
                this.moveBy(0.0, heave, sway);
            }
        }
    }
}
