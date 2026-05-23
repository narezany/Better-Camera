package nrz.bettercam.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import nrz.bettercam.client.CameraState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin {

    @Shadow
    private Vec2 modifyInput(Vec2 input) {
        return null;
    }

    @Unique private float savedYaw;
    @Unique private float savedPitch;
    @Unique private float savedLastYaw;
    @Unique private float savedLastPitch;

    @Inject(method = "applyInput", at = @At("HEAD"), cancellable = true)
    private void onTickMovementInput(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        Minecraft client = Minecraft.getInstance();
        
        if (client.getCameraEntity() == player && client.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            boolean isMoving = player.input.getMoveVector().lengthSquared() > 1.0E-5F;
            boolean isInteracting = client.options.keyAttack.isDown() 
                || client.options.keyUse.isDown() 
                || (client.gameMode != null && client.gameMode.isDestroying())
                || player.isUsingItem();
            
            if (CameraState.shiftLock) {
                // Shift Lock mode: player body constantly faces camera yaw
                float targetPlayerYaw = CameraState.cameraYaw;
                float currentYaw = player.getYRot();
                float diffYaw = Mth.wrapDegrees(targetPlayerYaw - currentYaw);
                float factor = nrz.bettercam.ModConfig.INSTANCE.enableTransitions ? 0.3F : 1.0F;
                float newYaw = currentYaw + diffYaw * factor;
                player.setYRot(newYaw);
                player.yHeadRot = newYaw;
                player.yBodyRot = newYaw;
                
                // Keep look pitch flat/horizontal or match camera pitch slightly
                float currentPitch = player.getXRot();
                float pitchFactor = nrz.bettercam.ModConfig.INSTANCE.enableTransitions ? 0.25F : 1.0F;
                player.setXRot(currentPitch + (0.0F - currentPitch) * pitchFactor);
                
                // Allow full strafe speeds
                Vec2 movementInput = player.input.getMoveVector();
                Vec2 vec2 = this.modifyInput(movementInput);
                player.xxa = vec2.x;
                player.zza = vec2.y;
                
                ((LivingEntityAccessor) player).setJumpingCam(player.input.keyPresses.jump());
                
                ci.cancel();
                return;
            }
            
            if (isInteracting) {
                float currentYaw = player.getYRot();
                float diffYaw = Mth.wrapDegrees(CameraState.cameraYaw - currentYaw);
                float factor = nrz.bettercam.ModConfig.INSTANCE.enableTransitions ? 0.3F : 1.0F;
                player.setYRot(currentYaw + diffYaw * factor);
                
                float currentPitch = player.getXRot();
                float diffPitch = CameraState.cameraPitch - currentPitch;
                player.setXRot(currentPitch + diffPitch * factor);
                
                player.yHeadRot = player.getYRot();
                player.yBodyRot = player.getYRot();
            } else if (isMoving) {
                Vec2 movementInput = player.input.getMoveVector();
                float sideways = movementInput.x;
                float forward = movementInput.y;
                float angleOffsetDegrees = (float) Math.toDegrees(Math.atan2(sideways, forward));
                float targetPlayerYaw = CameraState.cameraYaw - angleOffsetDegrees;
                
                float currentYaw = player.getYRot();
                float diffYaw = Mth.wrapDegrees(targetPlayerYaw - currentYaw);
                float factor = nrz.bettercam.ModConfig.INSTANCE.enableTransitions ? 0.25F : 1.0F;
                float newYaw = currentYaw + diffYaw * factor;
                player.setYRot(newYaw);
                player.yHeadRot = newYaw;
                player.yBodyRot = newYaw;
                
                float currentPitch = player.getXRot();
                player.setXRot(currentPitch + (0.0F - currentPitch) * factor);
            }
            
            if (isMoving) {
                Vec2 vec2 = this.modifyInput(new Vec2(0.0f, player.input.getMoveVector().length()));
                player.xxa = vec2.x;
                player.zza = vec2.y;
            } else {
                player.xxa = 0.0F;
                player.zza = 0.0F;
            }
            ((LivingEntityAccessor) player).setJumpingCam(player.input.keyPresses.jump());
            
            ci.cancel();
        }
    }

    @Unique
    private HitResult raycastFromCamera(Minecraft client, Entity cameraEntity, double maxDistance, float tickProgress, boolean includeFluids) {
        Vec3 cameraPos = client.gameRenderer.getMainCamera().position();
        Vec3 rotationVec = cameraEntity.calculateViewVector(CameraState.cameraPitch, CameraState.cameraYaw);
        Vec3 targetPos = cameraPos.add(rotationVec.x * maxDistance, rotationVec.y * maxDistance, rotationVec.z * maxDistance);
        
        return cameraEntity.level().clip(
            new ClipContext(
                cameraPos,
                targetPos,
                ClipContext.Block.COLLIDER,
                includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                cameraEntity
            )
        );
    }

    @Inject(method = "raycastHitResult", at = @At("HEAD"), cancellable = true)
    private void onGetCrosshairTargetHead(float tickProgress, Entity cameraEntity, CallbackInfoReturnable<HitResult> cir) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        Minecraft client = Minecraft.getInstance();
        if (client.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            if (CameraState.shiftLock) {
                double blockInteractionRange = player.blockInteractionRange();
                double entityInteractionRange = player.entityInteractionRange();
                
                double reach = Math.max(blockInteractionRange, entityInteractionRange);
                Vec3 cameraPos = client.gameRenderer.getMainCamera().position();
                Vec3 playerEyePos = player.getEyePosition(tickProgress);
                double camToPlayerDist = cameraPos.distanceTo(playerEyePos);
                double maxDistance = camToPlayerDist + reach;
                
                HitResult blockHitResult = raycastFromCamera(client, cameraEntity, maxDistance, tickProgress, false);
                
                double entityRaycastLimit = maxDistance;
                if (blockHitResult.getType() != HitResult.Type.MISS) {
                    entityRaycastLimit = blockHitResult.getLocation().distanceTo(cameraPos);
                }
                
                Vec3 rotationVec = cameraEntity.calculateViewVector(CameraState.cameraPitch, CameraState.cameraYaw);
                Vec3 targetPos = cameraPos.add(rotationVec.x * entityRaycastLimit, rotationVec.y * entityRaycastLimit, rotationVec.z * entityRaycastLimit);
                AABB box = cameraEntity.getBoundingBox().expandTowards(rotationVec.scale(entityRaycastLimit)).inflate(1.0, 1.0, 1.0);
                
                EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(cameraEntity, cameraPos, targetPos, box, EntitySelector.NO_SPECTATORS, entityRaycastLimit * entityRaycastLimit);
                
                if (entityHitResult != null) {
                    double entityDistToPlayer = entityHitResult.getLocation().distanceTo(playerEyePos);
                    if (entityDistToPlayer <= entityInteractionRange) {
                        cir.setReturnValue(entityHitResult);
                        return;
                    }
                }
                
                if (blockHitResult.getType() != HitResult.Type.MISS) {
                    double blockDistToPlayer = blockHitResult.getLocation().distanceTo(playerEyePos);
                    if (blockDistToPlayer <= blockInteractionRange) {
                        cir.setReturnValue(blockHitResult);
                        return;
                    }
                }
                
                // Return a clean missed result if nothing was in reach
                Vec3 missPos = cameraPos.add(rotationVec.scale(maxDistance));
                cir.setReturnValue(BlockHitResult.miss(missPos, Direction.UP, BlockPos.containing(missPos)));
                return;
            }

            this.savedYaw = player.getYRot();
            this.savedPitch = player.getXRot();
            this.savedLastYaw = player.yRotO;
            this.savedLastPitch = player.xRotO;
            
            player.setYRot(CameraState.cameraYaw);
            player.setXRot(CameraState.cameraPitch);
            player.yRotO = CameraState.cameraYaw;
            player.xRotO = CameraState.cameraPitch;
        }
    }

    @Inject(method = "raycastHitResult", at = @At("RETURN"))
    private void onGetCrosshairTargetReturn(float tickProgress, Entity cameraEntity, CallbackInfoReturnable<HitResult> cir) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        Minecraft client = Minecraft.getInstance();
        if (client.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
            if (CameraState.shiftLock) return;
            player.setYRot(this.savedYaw);
            player.setXRot(this.savedPitch);
            player.yRotO = this.savedLastYaw;
            player.xRotO = this.savedLastPitch;
        }
    }
}
