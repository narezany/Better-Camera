package nrz.bettercam.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import nrz.bettercam.client.CameraState;
import nrz.bettercam.client.CameraZoomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public class PlayerEntityModelMixin {
    @Inject(method = "setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V", at = @At("HEAD"))
    private void onSetAngles(PlayerEntityRenderState state, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null) {
            return;
        }
        if (client.player != null && state.id == client.player.getId()) {
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
