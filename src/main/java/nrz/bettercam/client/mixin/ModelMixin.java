package nrz.bettercam.client.mixin;

import net.minecraft.client.model.Model;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Model.class)
public class ModelMixin {
    @ModifyVariable(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V", at = @At("HEAD"), argsOnly = true, ordinal = 2)
    private int modifyModelColor(int originalColor) {
        if (nrz.bettercam.client.CameraState.isPlayerRendering) {
            int originalAlpha = (originalColor >> 24) & 0xFF;
            int newAlpha = (int) (originalAlpha * nrz.bettercam.client.CameraState.playerRenderAlpha);
            return (newAlpha << 24) | (originalColor & 0x00FFFFFF);
        }
        return originalColor;
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V", at = @At("RETURN"))
    private void onRenderReturn(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        nrz.bettercam.client.CameraState.isPlayerRendering = false;
    }
}
