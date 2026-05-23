package nrz.bettercam.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import nrz.bettercam.client.CameraState;
import nrz.bettercam.client.CameraZoomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.render.RenderLayer;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(LivingEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        CameraState.isPlayerRendering = false;
        CameraState.playerRenderAlpha = 1.0f;
        
        if (state instanceof PlayerEntityRenderState) {
            PlayerEntityRenderState playerState = (PlayerEntityRenderState) state;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen != null) {
                return;
            }
            if (client.player != null && playerState.id == client.player.getId()) {
                if (client.options.getPerspective().isFirstPerson()) {
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

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderReturn(LivingEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        CameraState.isPlayerRendering = false;
    }

    @ModifyVariable(method = "getRenderLayer", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private boolean modifyTranslucent(boolean originalTranslucent, LivingEntityRenderState state) {
        if (state instanceof PlayerEntityRenderState) {
            PlayerEntityRenderState playerState = (PlayerEntityRenderState) state;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen != null) {
                return originalTranslucent;
            }
            if (client.player != null && playerState.id == client.player.getId()) {
                if (client.options.getPerspective().isFirstPerson()) {
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

    @Inject(method = "shouldRenderFeatures", at = @At("HEAD"), cancellable = true)
    private void onShouldRenderFeatures(LivingEntityRenderState state, CallbackInfoReturnable<Boolean> cir) {
        if (state instanceof PlayerEntityRenderState) {
            PlayerEntityRenderState playerState = (PlayerEntityRenderState) state;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen != null) {
                return;
            }
            if (client.player != null && playerState.id == client.player.getId()) {
                if (client.options.getPerspective().isFirstPerson()) {
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
