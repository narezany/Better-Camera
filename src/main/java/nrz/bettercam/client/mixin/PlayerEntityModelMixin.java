package nrz.bettercam.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import nrz.bettercam.client.CameraState;
import nrz.bettercam.client.CameraZoomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerEntityModelMixin {
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("HEAD"))
    private void onSetAngles(AvatarRenderState state, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen != null) {
            return;
        }
        if (client.player != null) {
            if (state.id == client.player.getId()) {
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
}
