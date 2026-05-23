package nrz.bettercam.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;

public class CameraZoomState {
    public static float targetDistance = 4.0F;
    public static float currentDistance = 4.0F;
    public static float preferredDistance = 4.0F; // Remembers preferred zoom when scrolling
    public static long lastTime = 0;

    public static boolean transitioningToFirstPerson = false;
    public static boolean forceSetPerspective = false; // Bypass flag to avoid recursion in GameOptionsMixin

    public static void zoom(double amount) {
        Minecraft client = Minecraft.getInstance();
        if (client.options.getCameraType() == CameraType.FIRST_PERSON) {
            if (amount < 0) { // Scroll down (zoom out)
                // Switch to third person back smoothly
                forceSetPerspective = true;
                client.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                forceSetPerspective = false;
                
                CameraState.initialize();
                currentDistance = 0.0F;
                targetDistance = 1.8F;
                preferredDistance = 1.8F;
            }
            return;
        }

        // Scroll up (positive amount) zooms in (decreases distance)
        // Scroll down (negative amount) zooms out (increases distance)
        targetDistance -= (float) (amount * 0.75f);
        if (targetDistance < 0.0F) targetDistance = 0.0F;
        if (targetDistance > 15.0F) targetDistance = 15.0F;

        if (targetDistance > 0.0F) {
            preferredDistance = targetDistance;
            transitioningToFirstPerson = false; // Cancel transition if scrolling back out
        }
    }

    public static void update() {
        float actualTarget = transitioningToFirstPerson ? 0.0F : targetDistance;

        if (!nrz.bettercam.ModConfig.INSTANCE.enableTransitions) {
            currentDistance = actualTarget;
        } else {
            long now = System.currentTimeMillis();
            if (lastTime == 0) lastTime = now;
            float dt = (now - lastTime) / 1000.0f;
            lastTime = now;

            if (dt > 0.1f) dt = 0.1f; // clamp to prevent jumps

            // Speed up the transition when moving to first-person for responsiveness
            float speed = (actualTarget == 0.0F) ? 18.0f : 8.0f;
            currentDistance = (float) (actualTarget + (currentDistance - actualTarget) * Math.exp(-speed * dt));
        }

        // When zoom enters the extreme close threshold, actually set game perspective to first person
        if (currentDistance < 0.08f && actualTarget == 0.0F) {
            Minecraft client = Minecraft.getInstance();
            if (client.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
                if (client.player != null) {
                    client.player.setYRot(CameraState.cameraYaw);
                    client.player.setXRot(CameraState.cameraPitch);
                    client.player.yRotO = CameraState.cameraYaw;
                    client.player.xRotO = CameraState.cameraPitch;
                    client.player.yHeadRot = CameraState.cameraYaw;
                    client.player.yBodyRot = CameraState.cameraYaw;
                }
                forceSetPerspective = true;
                client.options.setCameraType(CameraType.FIRST_PERSON);
                forceSetPerspective = false;
                transitioningToFirstPerson = false;
            }
        }
    }
}
