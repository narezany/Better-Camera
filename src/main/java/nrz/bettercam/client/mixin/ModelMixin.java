package nrz.bettercam.client.mixin;

import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModelPart.class)
public class ModelMixin {
    @ModifyVariable(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V", at = @At("HEAD"), argsOnly = true, ordinal = 3)
    private float modifyModelAlpha(float originalAlpha) {
        if (nrz.bettercam.client.CameraState.isPlayerRendering) {
            return originalAlpha * nrz.bettercam.client.CameraState.playerRenderAlpha;
        }
        return originalAlpha;
    }
}
