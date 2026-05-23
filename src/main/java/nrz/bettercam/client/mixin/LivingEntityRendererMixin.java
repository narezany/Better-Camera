package nrz.bettercam.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import nrz.bettercam.client.CameraState;
import nrz.bettercam.client.CameraZoomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "submit", at = @At("HEAD"))
    private void onRenderHead(LivingEntityRenderState state, PoseStack matrices, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState, CallbackInfo ci) {
        CameraState.isPlayerRendering = false;
        CameraState.playerRenderAlpha = 1.0f;
        
        if (state instanceof AvatarRenderState) {
            AvatarRenderState playerState = (AvatarRenderState) state;
            Minecraft client = Minecraft.getInstance();
            if (client.screen != null) {
                return;
            }
            if (client.player != null && playerState.id == client.player.getId()) {
                if (client.options.getCameraType().isFirstPerson()) {
                    return;
                }
                
                float dist = CameraZoomState.currentDistance;
                if (dist < 1.6f) {
                    CameraState.isPlayerRendering = true;
                    if (dist < 0.4f) {
                        CameraState.playerRenderAlpha = 0.0f;
                    } else {
                        CameraState.playerRenderAlpha = (dist - 0.4f) / (1.6f - 0.4f);
                    }
                }
            }
        }
    }

    @Inject(method = "submit", at = @At("RETURN"))
    private void onRenderReturn(LivingEntityRenderState state, PoseStack matrices, net.minecraft.client.renderer.SubmitNodeCollector submitNodeCollector, net.minecraft.client.renderer.state.level.CameraRenderState cameraRenderState, CallbackInfo ci) {
        CameraState.isPlayerRendering = false;
    }

    @ModifyVariable(method = "getRenderType", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private boolean modifyTranslucent(boolean originalTranslucent, LivingEntityRenderState state) {
        if (state instanceof AvatarRenderState) {
            AvatarRenderState playerState = (AvatarRenderState) state;
            Minecraft client = Minecraft.getInstance();
            if (client.screen != null) {
                return originalTranslucent;
            }
            if (client.player != null && playerState.id == client.player.getId()) {
                if (client.options.getCameraType().isFirstPerson()) {
                    return originalTranslucent;
                }
                float dist = CameraZoomState.currentDistance;
                if (dist < 1.6f) {
                    return true; // Force translucent render layer
                }
            }
        }
        return originalTranslucent;
    }

    @Inject(method = "shouldRenderLayers", at = @At("HEAD"), cancellable = true)
    private void onShouldRenderFeatures(LivingEntityRenderState state, CallbackInfoReturnable<Boolean> cir) {
        if (state instanceof AvatarRenderState) {
            AvatarRenderState playerState = (AvatarRenderState) state;
            Minecraft client = Minecraft.getInstance();
            if (client.screen != null) {
                return;
            }
            if (client.player != null && playerState.id == client.player.getId()) {
                if (client.options.getCameraType().isFirstPerson()) {
                    return;
                }
                float dist = CameraZoomState.currentDistance;
                if (dist < 1.6f) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
