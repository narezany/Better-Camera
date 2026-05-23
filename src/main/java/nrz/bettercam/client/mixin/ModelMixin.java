package nrz.bettercam.client.mixin;

import net.minecraft.client.model.Model;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Model.class)
public class ModelMixin {
    @ModifyVariable(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private int modifyModelColor(int originalColor) {
        if (nrz.bettercam.client.CameraState.isPlayerRendering) {
            int originalAlpha = (originalColor >> 24) & 0xFF;
            int newAlpha = (int) (originalAlpha * nrz.bettercam.client.CameraState.playerRenderAlpha);
            return (newAlpha << 24) | (originalColor & 0x00FFFFFF);
        }
        return originalColor;
    }

    @Inject(method = "renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("RETURN"))
    private void onRenderReturn(CallbackInfo ci) {
        nrz.bettercam.client.CameraState.isPlayerRendering = false;
    }
}
