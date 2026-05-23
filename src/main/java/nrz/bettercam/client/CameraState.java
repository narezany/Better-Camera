package nrz.bettercam.client;

import net.minecraft.client.Minecraft;

public class CameraState {
    public static float cameraYaw = 0.0f;
    public static float cameraPitch = 0.0f;
    public static boolean initialized = false;
    public static boolean shiftLock = false;
    
    public static float currentShiftLockAlpha = 0.0f;
    public static long lastTime = 0;
    
    public static boolean isPlayerRendering = false;
    public static float playerRenderAlpha = 1.0f;

    public static float hudSwayX = 0.0f;
    public static float hudSwayY = 0.0f;
    public static float lastCameraYaw = 0.0f;
    public static float lastCameraPitch = 0.0f;
    public static boolean lastCameraInitialized = false;

    public static void rotateCamera(double dx, double dy) {
        if (!initialized) {
            initialize();
        }
        cameraYaw += (float) dx * 0.15f;
        cameraPitch += (float) dy * 0.15f;

        // Clamp pitch to avoid flipping upside down (-89 to 89 degrees)
        if (cameraPitch < -89.0f) cameraPitch = -89.0f;
        if (cameraPitch > 89.0f) cameraPitch = 89.0f;
    }

    public static void initialize() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            cameraYaw = client.player.getYRot();
            cameraPitch = client.player.getXRot();
            initialized = true;
            lastTime = 0;
            CameraZoomState.lastTime = 0;
        }
    }

    public static void updateTransition() {
        float target = (shiftLock && initialized) ? 1.0f : 0.0f;
        if (!nrz.bettercam.ModConfig.INSTANCE.enableTransitions) {
            currentShiftLockAlpha = target;
            return;
        }

        long now = System.currentTimeMillis();
        if (lastTime == 0) lastTime = now;
        float dt = (now - lastTime) / 1000.0f;
        lastTime = now;

        if (dt > 0.1f) dt = 0.1f;

        float speed = 12.0f; // fast but very smooth
        currentShiftLockAlpha = (float) (target + (currentShiftLockAlpha - target) * Math.exp(-speed * dt));
    }
}
