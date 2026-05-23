package nrz.bettercam.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderTickCounter;
import nrz.bettercam.client.CameraState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (this.client.gameRenderer != null && this.client.gameRenderer.getCamera() != null) {
            float currentYaw = this.client.gameRenderer.getCamera().getYaw();
            float currentPitch = this.client.gameRenderer.getCamera().getPitch();
            
            if (CameraState.lastCameraInitialized) {
                float diffYaw = net.minecraft.util.math.MathHelper.wrapDegrees(currentYaw - CameraState.lastCameraYaw);
                float diffPitch = currentPitch - CameraState.lastCameraPitch;
                
                float strength = nrz.bettercam.ModConfig.INSTANCE.hudSwayStrength;
                float multiplier = strength * 0.4f;
                
                CameraState.hudSwayX += diffYaw * multiplier;
                CameraState.hudSwayY -= diffPitch * multiplier;
                
                // Clamp to prevent excessive shifting
                CameraState.hudSwayX = net.minecraft.util.math.MathHelper.clamp(CameraState.hudSwayX, -15.0f, 15.0f);
                CameraState.hudSwayY = net.minecraft.util.math.MathHelper.clamp(CameraState.hudSwayY, -10.0f, 10.0f);
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
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(CameraState.hudSwayX, CameraState.hudSwayY);
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderReturn(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (nrz.bettercam.ModConfig.INSTANCE.hudSwayStrength > 0.001f) {
            context.getMatrices().popMatrix();
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void onRenderCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (nrz.bettercam.ModConfig.INSTANCE.hudSwayStrength > 0.001f) {
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(-CameraState.hudSwayX, -CameraState.hudSwayY);
        }

        if (this.client.options.getPerspective() == Perspective.THIRD_PERSON_BACK) {
            float alpha = CameraState.currentShiftLockAlpha;
            if (alpha > 0.001f) {
                int cx = context.getScaledWindowWidth() / 2;
                int cy = context.getScaledWindowHeight() / 2;
                
                drawRobloxCursor(context, cx, cy, alpha);
                
                if (nrz.bettercam.ModConfig.INSTANCE.hudSwayStrength > 0.001f) {
                    context.getMatrices().popMatrix();
                }
                ci.cancel();
                return;
            }
        }
    }

    @Inject(method = "renderCrosshair", at = @At("RETURN"))
    private void onRenderCrosshairReturn(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (nrz.bettercam.ModConfig.INSTANCE.hudSwayStrength > 0.001f) {
            context.getMatrices().popMatrix();
        }
    }
    
    @Unique
    private void drawRobloxCursor(DrawContext context, int cx, int cy, float alpha) {
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
