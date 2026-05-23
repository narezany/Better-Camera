package nrz.bettercam.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import nrz.bettercam.client.CameraState;
import nrz.bettercam.client.CameraZoomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumerProvider;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"))
    private void onRenderHead(LivingEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        CameraState.isPlayerRendering = false;
        CameraState.playerRenderAlpha = 1.0f;
        
        if (entity instanceof PlayerEntity) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen != null) {
                return;
            }
            if (client.player != null && entity.getId() == client.player.getId()) {
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

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("RETURN"))
    private void onRenderReturn(LivingEntity entity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        CameraState.isPlayerRendering = false;
    }

    @ModifyVariable(method = "getRenderLayer", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private boolean modifyTranslucent(boolean originalTranslucent, LivingEntity entity) {
        if (entity instanceof PlayerEntity) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.currentScreen != null) {
                return originalTranslucent;
            }
            if (client.player != null && entity.getId() == client.player.getId()) {
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
}
