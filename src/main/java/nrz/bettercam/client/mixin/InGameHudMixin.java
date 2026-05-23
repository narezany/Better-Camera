package nrz.bettercam.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import nrz.bettercam.client.CameraState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void onRenderHead(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (this.minecraft.gameRenderer != null && this.minecraft.gameRenderer.getMainCamera() != null) {
            float currentYaw = this.minecraft.gameRenderer.getMainCamera().yRot();
            float currentPitch = this.minecraft.gameRenderer.getMainCamera().xRot();
            
            if (CameraState.lastCameraInitialized) {
                float diffYaw = net.minecraft.util.Mth.wrapDegrees(currentYaw - CameraState.lastCameraYaw);
                float diffPitch = currentPitch - CameraState.lastCameraPitch;
                
                float strength = nrz.bettercam.ModConfig.INSTANCE.hudSwayStrength;
                float multiplier = strength * 0.4f;
                
                CameraState.hudSwayX += diffYaw * multiplier;
                CameraState.hudSwayY -= diffPitch * multiplier;
                
                // Clamp to prevent excessive shifting
                CameraState.hudSwayX = net.minecraft.util.Mth.clamp(CameraState.hudSwayX, -15.0f, 15.0f);
                CameraState.hudSwayY = net.minecraft.util.Mth.clamp(CameraState.hudSwayY, -10.0f, 10.0f);
            } else {
                CameraState.lastCameraInitialized = true;
            }
            
            CameraState.lastCameraYaw = currentYaw;
            CameraState.lastCameraPitch = currentPitch;
        }
        
        // Decay the sway back to center
        CameraState.hudSwayX *= 0.85f;
        CameraState.hudSwayY *= 0.85f;
        
        if (nrz.bettercam.ModConfig.INSTANCE.hudSwayStrength > 0.001f) {
            context.pose().pushMatrix();
            context.pose().translate(CameraState.hudSwayX, CameraState.hudSwayY);
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void onRenderReturn(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (nrz.bettercam.ModConfig.INSTANCE.hudSwayStrength > 0.001f) {
            context.pose().popMatrix();
        }
    }

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (nrz.bettercam.ModConfig.INSTANCE.hudSwayStrength > 0.001f) {
            context.pose().pushMatrix();
            context.pose().translate(-CameraState.hudSwayX, -CameraState.hudSwayY);
        }

        if (this.minecraft.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            float alpha = CameraState.currentShiftLockAlpha;
            if (alpha > 0.001f) {
                int cx = context.guiWidth() / 2;
                int cy = context.guiHeight() / 2;
                
                drawRobloxCursor(context, cx, cy, alpha);
                
                if (nrz.bettercam.ModConfig.INSTANCE.hudSwayStrength > 0.001f) {
                    context.pose().popMatrix();
                }
                ci.cancel();
                return;
            }
        }
    }

    @Inject(method = "extractCrosshair", at = @At("RETURN"))
    private void onRenderCrosshairReturn(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (nrz.bettercam.ModConfig.INSTANCE.hudSwayStrength > 0.001f) {
            context.pose().popMatrix();
        }
    }
    
    @Unique
    private void drawRobloxCursor(GuiGraphicsExtractor context, int cx, int cy, float alpha) {
        int alphaValue = (int) (170 * alpha); // 170 is 0xAA
        int dotAlphaValue = (int) (221 * alpha); // 221 is 0xDD
        
        int color = (alphaValue << 24) | 0x00FFFFFF;
        int dotColor = (dotAlphaValue << 24) | 0x00FFFFFF;
        
        context.fill(cx - 1, cy - 3, cx + 2, cy - 2, color); // top
        context.fill(cx - 1, cy + 3, cx + 2, cy + 4, color); // bottom
        context.fill(cx - 3, cy - 1, cx - 2, cy + 2, color); // left
        context.fill(cx + 3, cy - 1, cx + 4, cy + 2, color); // right
        
        // Corners
        context.fill(cx - 2, cy - 2, cx - 1, cy - 1, color);
        context.fill(cx + 2, cy - 2, cx + 3, cy - 1, color);
        context.fill(cx - 2, cy + 2, cx - 1, cy + 3, color);
        context.fill(cx + 2, cy + 2, cx + 3, cy + 3, color);
        
        // Center dot
        context.fill(cx, cy, cx + 1, cy + 1, dotColor);
    }
}
