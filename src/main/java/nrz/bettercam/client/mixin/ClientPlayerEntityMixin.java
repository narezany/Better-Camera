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

    @Unique private float savedYaw;
    @Unique private float savedPitch;
    @Unique private float savedLastYaw;
    @Unique private float savedLastPitch;

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

    @Unique
    private HitResult raycastFromCamera(MinecraftClient client, Entity cameraEntity, double maxDistance, float tickProgress, boolean includeFluids) {
        Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
        Vec3d rotationVec = cameraEntity.getRotationVector(CameraState.cameraPitch, CameraState.cameraYaw);
        Vec3d targetPos = cameraPos.add(rotationVec.x * maxDistance, rotationVec.y * maxDistance, rotationVec.z * maxDistance);
        
        return cameraEntity.getEntityWorld().raycast(
            new RaycastContext(
                cameraPos,
                targetPos,
                RaycastContext.ShapeType.OUTLINE,
                includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE,
                cameraEntity
            )
        );
    }

    @Inject(method = "getCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void onGetCrosshairTargetHead(float tickProgress, Entity cameraEntity, CallbackInfoReturnable<HitResult> cir) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.getPerspective() == Perspective.THIRD_PERSON_BACK) {
            if (CameraState.shiftLock) {
                double blockInteractionRange = player.getBlockInteractionRange();
                double entityInteractionRange = player.getEntityInteractionRange();
                
                double reach = Math.max(blockInteractionRange, entityInteractionRange);
                Vec3d cameraPos = client.gameRenderer.getCamera().getCameraPos();
                Vec3d playerEyePos = player.getCameraPosVec(tickProgress);
                double camToPlayerDist = cameraPos.distanceTo(playerEyePos);
                double maxDistance = camToPlayerDist + reach;
                
                HitResult blockHitResult = raycastFromCamera(client, cameraEntity, maxDistance, tickProgress, false);
                
                double entityRaycastLimit = maxDistance;
                if (blockHitResult.getType() != HitResult.Type.MISS) {
                    entityRaycastLimit = blockHitResult.getPos().distanceTo(cameraPos);
                }
                
                Vec3d rotationVec = cameraEntity.getRotationVector(CameraState.cameraPitch, CameraState.cameraYaw);
                Vec3d targetPos = cameraPos.add(rotationVec.x * entityRaycastLimit, rotationVec.y * entityRaycastLimit, rotationVec.z * entityRaycastLimit);
                Box box = cameraEntity.getBoundingBox().stretch(rotationVec.multiply(entityRaycastLimit)).expand(1.0, 1.0, 1.0);
                
                EntityHitResult entityHitResult = ProjectileUtil.raycast(cameraEntity, cameraPos, targetPos, box, EntityPredicates.CAN_HIT, entityRaycastLimit * entityRaycastLimit);
                
                if (entityHitResult != null) {
                    double entityDistToPlayer = entityHitResult.getPos().distanceTo(playerEyePos);
                    if (entityDistToPlayer <= entityInteractionRange) {
                        cir.setReturnValue(entityHitResult);
                        return;
                    }
                }
                
                if (blockHitResult.getType() != HitResult.Type.MISS) {
                    double blockDistToPlayer = blockHitResult.getPos().distanceTo(playerEyePos);
                    if (blockDistToPlayer <= blockInteractionRange) {
                        cir.setReturnValue(blockHitResult);
                        return;
                    }
                }
                
                // Return a clean missed result if nothing was in reach
                Vec3d missPos = cameraPos.add(rotationVec.multiply(maxDistance));
                cir.setReturnValue(BlockHitResult.createMissed(missPos, Direction.UP, BlockPos.ofFloored(missPos)));
                return;
            }

            this.savedYaw = player.getYaw();
            this.savedPitch = player.getPitch();
            this.savedLastYaw = player.lastYaw;
            this.savedLastPitch = player.lastPitch;
            
            player.setYaw(CameraState.cameraYaw);
            player.setPitch(CameraState.cameraPitch);
            player.lastYaw = CameraState.cameraYaw;
            player.lastPitch = CameraState.cameraPitch;
        }
    }

    @Inject(method = "getCrosshairTarget", at = @At("RETURN"))
    private void onGetCrosshairTargetReturn(float tickProgress, Entity cameraEntity, CallbackInfoReturnable<HitResult> cir) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.getPerspective() == Perspective.THIRD_PERSON_BACK) {
            if (CameraState.shiftLock) return;
            player.setYaw(this.savedYaw);
            player.setPitch(this.savedPitch);
            player.lastYaw = this.savedLastYaw;
            player.lastPitch = this.savedLastPitch;
        }
    }
}
