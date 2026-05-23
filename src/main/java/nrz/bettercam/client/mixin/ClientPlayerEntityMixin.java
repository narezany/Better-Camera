package nrz.bettercam.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import nrz.bettercam.client.CameraState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.Box;
import net.minecraft.world.RaycastContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Shadow
    private Vec2f applyMovementSpeedFactors(Vec2f input) {
        return null;
    }


    @Inject(method = "tickMovementInput", at = @At("HEAD"), cancellable = true)
    private void onTickMovementInput(CallbackInfo ci) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        
        if (client.getCameraEntity() == player && client.options.getPerspective() == Perspective.THIRD_PERSON_BACK) {
            boolean isMoving = player.input.getMovementInput().lengthSquared() > 1.0E-5F;
            boolean isInteracting = client.options.attackKey.isPressed() 
                || client.options.useKey.isPressed() 
                || (client.interactionManager != null && client.interactionManager.isBreakingBlock())
                || player.isUsingItem();
            
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
                
                // Keep look pitch flat/horizontal or match camera pitch slightly (Roblox matches camera pitch slightly, but flat is cleaner in MC)
                float currentPitch = player.getPitch();
                float pitchFactor = nrz.bettercam.ModConfig.INSTANCE.enableTransitions ? 0.25F : 1.0F;
                player.setPitch(currentPitch + (0.0F - currentPitch) * pitchFactor);
                
                // Allow full strafe speeds
                Vec2f movementInput = player.input.getMovementInput();
                Vec2f vec2f = this.applyMovementSpeedFactors(movementInput);
                player.sidewaysSpeed = vec2f.x;
                player.forwardSpeed = vec2f.y;
                
                ((LivingEntityAccessor) player).setJumping(player.input.playerInput.jump());
                
                player.lastRenderYaw = player.renderYaw;
                player.lastRenderPitch = player.renderPitch;
                if (nrz.bettercam.ModConfig.INSTANCE.enableTransitions) {
                    player.renderPitch = player.renderPitch + (player.getPitch() - player.renderPitch) * 0.5F;
                    player.renderYaw = player.renderYaw + (player.getYaw() - player.renderYaw) * 0.5F;
                } else {
                    player.renderPitch = player.getPitch();
                    player.renderYaw = player.getYaw();
                }
                
                ci.cancel();
                return;
            }
            
            if (isInteracting) {
                float currentYaw = player.getYaw();
                float diffYaw = MathHelper.wrapDegrees(CameraState.cameraYaw - currentYaw);
                float factor = nrz.bettercam.ModConfig.INSTANCE.enableTransitions ? 0.3F : 1.0F;
                player.setYaw(currentYaw + diffYaw * factor);
                
                float currentPitch = player.getPitch();
                float diffPitch = CameraState.cameraPitch - currentPitch;
                player.setPitch(currentPitch + diffPitch * factor);
                
                player.headYaw = player.getYaw();
                player.bodyYaw = player.getYaw();
            } else if (isMoving) {
                Vec2f movementInput = player.input.getMovementInput();
                float length = movementInput.length();
                float sideways = movementInput.x;
                float forward = movementInput.y;
                float angleOffsetDegrees = (float) Math.toDegrees(Math.atan2(sideways, forward));
                float targetPlayerYaw = CameraState.cameraYaw - angleOffsetDegrees;
                
                float currentYaw = player.getYaw();
                float diffYaw = MathHelper.wrapDegrees(targetPlayerYaw - currentYaw);
                float factor = nrz.bettercam.ModConfig.INSTANCE.enableTransitions ? 0.25F : 1.0F;
                float newYaw = currentYaw + diffYaw * factor;
                player.setYaw(newYaw);
                player.headYaw = newYaw;
                player.bodyYaw = newYaw;
                
                float currentPitch = player.getPitch();
                player.setPitch(currentPitch + (0.0F - currentPitch) * factor);
            }
            
            if (isMoving) {
                Vec2f vec2f = this.applyMovementSpeedFactors(new Vec2f(0.0f, player.input.getMovementInput().length()));
                player.sidewaysSpeed = vec2f.x;
                player.forwardSpeed = vec2f.y;
            } else {
                player.sidewaysSpeed = 0.0F;
                player.forwardSpeed = 0.0F;
            }
            ((LivingEntityAccessor) player).setJumping(player.input.playerInput.jump());
            
            player.lastRenderYaw = player.renderYaw;
            player.lastRenderPitch = player.renderPitch;
            if (nrz.bettercam.ModConfig.INSTANCE.enableTransitions) {
                player.renderPitch = player.renderPitch + (player.getPitch() - player.renderPitch) * 0.5F;
                player.renderYaw = player.renderYaw + (player.getYaw() - player.renderYaw) * 0.5F;
            } else {
                player.renderPitch = player.getPitch();
                player.renderYaw = player.getYaw();
            }
            
            ci.cancel();
        }
    }
}
