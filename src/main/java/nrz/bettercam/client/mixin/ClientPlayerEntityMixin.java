package nrz.bettercam.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import nrz.bettercam.client.CameraState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Unique
    private Vec2f applyMovementSpeedFactors(Vec2f input) {
        float f = input.x;
        float g = input.y;
        float h = 1.0F;
        return new Vec2f(f * h, g * h);
    }

    @Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tickMovement()V"))
    private void onTickMovement(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        
        if (client.options.getPerspective() == Perspective.THIRD_PERSON_BACK) {
            if (CameraState.shiftLock) {
                // Shift Lock mode: player body constantly faces camera yaw
                float targetPlayerYaw = CameraState.cameraYaw;
                float currentYaw = player.getYaw();
                float diffYaw = MathHelper.wrapDegrees(targetPlayerYaw - currentYaw);
                float factor = nrz.bettercam.ModConfig.INSTANCE.enableTransitions ? 0.3F : 1.0F;
                float newYaw = currentYaw + diffYaw * factor;
                player.setYaw(newYaw);
                player.headYaw = newYaw;
                player.bodyYaw = newYaw;
                
                // Keep look pitch flat/horizontal
                float currentPitch = player.getPitch();
                float pitchFactor = nrz.bettercam.ModConfig.INSTANCE.enableTransitions ? 0.25F : 1.0F;
                player.setPitch(currentPitch + (0.0F - currentPitch) * pitchFactor);
                
                // Allow full strafe speeds
                Vec2f movementInput = player.input.getMovementInput();
                Vec2f vec2f = this.applyMovementSpeedFactors(movementInput);
                player.sidewaysSpeed = vec2f.x;
                player.forwardSpeed = vec2f.y;
                
                ((LivingEntityAccessor) player).setJumping(player.input.jumping);
                
                player.lastRenderYaw = player.renderYaw;
                player.lastRenderPitch = player.renderPitch;
                if (nrz.bettercam.ModConfig.INSTANCE.enableTransitions) {
                    player.renderPitch = player.renderPitch + (player.getPitch() - player.renderPitch) * 0.5F;
                    player.renderYaw = player.renderYaw + (player.getYaw() - player.renderYaw) * 0.5F;
                } else {
                    player.renderPitch = player.getPitch();
                    player.renderYaw = player.getYaw();
                }
            }
            
            // Non-Shift Lock orbit camera behavior
            boolean isMoving = player.input.pressingForward || player.input.pressingBack || player.input.pressingLeft || player.input.pressingRight;
            
            if (isMoving) {
                // Determine movement direction relative to camera yaw
                float inputYaw = 0.0F;
                if (player.input.pressingForward) {
                    if (player.input.pressingLeft) inputYaw = -45.0F;
                    else if (player.input.pressingRight) inputYaw = 45.0F;
                    else inputYaw = 0.0F;
                } else if (player.input.pressingBack) {
                    if (player.input.pressingLeft) inputYaw = -135.0F;
                    else if (player.input.pressingRight) inputYaw = 135.0F;
                    else inputYaw = 180.0F;
                } else if (player.input.pressingLeft) {
                    inputYaw = -90.0F;
                } else if (player.input.pressingRight) {
                    inputYaw = 90.0F;
                }
                
                // Smoothly rotate the player's body to face the walking direction
                float targetPlayerYaw = CameraState.cameraYaw + inputYaw;
                float currentYaw = player.getYaw();
                float diffYaw = MathHelper.wrapDegrees(targetPlayerYaw - currentYaw);
                float factor = nrz.bettercam.ModConfig.INSTANCE.enableTransitions ? 0.25F : 1.0F;
                
                float newYaw = currentYaw + diffYaw * factor;
                player.setYaw(newYaw);
                player.headYaw = newYaw;
                player.bodyYaw = newYaw;
                
                // Set forward speed (Roblox turning converts strafing into forward movement)
                Vec2f vec2f = this.applyMovementSpeedFactors(new Vec2f(0.0f, player.input.getMovementInput().length()));
                player.sidewaysSpeed = vec2f.x;
                player.forwardSpeed = vec2f.y;
            } else {
                player.sidewaysSpeed = 0.0F;
                player.forwardSpeed = 0.0F;
            }
            ((LivingEntityAccessor) player).setJumping(player.input.jumping);
            
            player.lastRenderYaw = player.renderYaw;
            player.lastRenderPitch = player.renderPitch;
            if (nrz.bettercam.ModConfig.INSTANCE.enableTransitions) {
                player.renderPitch = player.renderPitch + (player.getPitch() - player.renderPitch) * 0.5F;
                player.renderYaw = player.renderYaw + (player.getYaw() - player.renderYaw) * 0.5F;
            } else {
                player.renderPitch = player.getPitch();
                player.renderYaw = player.getYaw();
            }
        }
    }
}
