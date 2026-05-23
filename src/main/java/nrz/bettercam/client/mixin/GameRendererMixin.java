package nrz.bettercam.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import nrz.bettercam.client.CameraState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "updateTargetedEntity", at = @At("HEAD"), cancellable = true)
    private void onUpdateTargetedEntity(float tickDelta, CallbackInfo ci) {
        if (this.client.options.getPerspective() == Perspective.THIRD_PERSON_BACK && CameraState.shiftLock && this.client.player != null) {
            Entity cameraEntity = this.client.getCameraEntity();
            if (cameraEntity != null) {
                double reach = this.client.player.isCreative() ? 5.0D : 3.0D;
                Vec3d cameraPos = this.client.gameRenderer.getCamera().getPos();
                Vec3d playerEyePos = this.client.player.getCameraPosVec(tickDelta);
                double camToPlayerDist = cameraPos.distanceTo(playerEyePos);
                double maxDistance = camToPlayerDist + reach;
                
                HitResult blockHitResult = raycastFromCamera(cameraEntity, maxDistance, tickDelta, false);
                
                double entityRaycastLimit = maxDistance;
                if (blockHitResult.getType() != HitResult.Type.MISS) {
                    entityRaycastLimit = blockHitResult.getPos().distanceTo(cameraPos);
                }
                
                Vec3d rotationVec = calculateRotationVector(CameraState.cameraPitch, CameraState.cameraYaw);
                Vec3d targetPos = cameraPos.add(rotationVec.x * entityRaycastLimit, rotationVec.y * entityRaycastLimit, rotationVec.z * entityRaycastLimit);
                Box box = cameraEntity.getBoundingBox().stretch(rotationVec.multiply(entityRaycastLimit)).expand(1.0, 1.0, 1.0);
                
                EntityHitResult entityHitResult = ProjectileUtil.raycast(
                    cameraEntity, 
                    cameraPos, 
                    targetPos, 
                    box, 
                    entity -> !entity.isSpectator() && entity.canHit(), 
                    entityRaycastLimit * entityRaycastLimit
                );
                
                if (entityHitResult != null) {
                    double entityDistToPlayer = entityHitResult.getPos().distanceTo(playerEyePos);
                    if (entityDistToPlayer <= reach) {
                        this.client.crosshairTarget = entityHitResult;
                        this.client.targetedEntity = entityHitResult.getEntity();
                        ci.cancel();
                        return;
                    }
                }
                
                if (blockHitResult.getType() != HitResult.Type.MISS) {
                    double blockDistToPlayer = blockHitResult.getPos().distanceTo(playerEyePos);
                    if (blockDistToPlayer <= reach) {
                        this.client.crosshairTarget = blockHitResult;
                        this.client.targetedEntity = null;
                        ci.cancel();
                        return;
                    }
                }
                
                // Return a clean missed result if nothing was in reach
                Vec3d missPos = cameraPos.add(rotationVec.multiply(maxDistance));
                this.client.crosshairTarget = BlockHitResult.createMissed(missPos, Direction.UP, BlockPos.ofFloored(missPos));
                this.client.targetedEntity = null;
                ci.cancel();
            }
        }
    }

    @org.spongepowered.asm.mixin.Unique
    private HitResult raycastFromCamera(Entity cameraEntity, double maxDistance, float tickProgress, boolean includeFluids) {
        Vec3d cameraPos = this.client.gameRenderer.getCamera().getPos();
        Vec3d rotationVec = calculateRotationVector(CameraState.cameraPitch, CameraState.cameraYaw);
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

    @org.spongepowered.asm.mixin.Unique
    private Vec3d calculateRotationVector(float pitch, float yaw) {
        float f = pitch * (float) (Math.PI / 180.0);
        float g = -yaw * (float) (Math.PI / 180.0);
        float h = net.minecraft.util.math.MathHelper.cos(g);
        float i = net.minecraft.util.math.MathHelper.sin(g);
        float j = net.minecraft.util.math.MathHelper.cos(f);
        float k = net.minecraft.util.math.MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }
}
