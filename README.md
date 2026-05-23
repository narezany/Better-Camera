# Better Camera (Roblox-style Camera & Shift Lock)

Better Camera is a Minecraft Fabric mod that overhauls the third-person camera perspective to function similarly to Roblox, featuring an over-the-shoulder Shift Lock mode, custom crosshair rendering, and smooth scroll zoom.

This branch contains the source code for **Minecraft 1.21.11**.

## Features

* **Roblox Orbit Camera**:
  * Cycles only between **First Person** and **Third Person Back** (skips standard front-view perspective) using the standard `F5` key.
  * Mouse movement rotates the camera *around* the player character without turning the player.
  * The player character only turns to face the movement direction when walking (using WASD).

* **Roblox Shift Lock (L-CTRL Toggle)**:
  * Press **Left Control** in third-person back view to toggle **Shift Lock**.
  * Offsets the camera slightly **up** and **to the right** (over-the-shoulder view).
  * Renders a custom **circular crosshair** dynamically in the center of the screen.
  * The player character constantly and smoothly rotates to face the direction the camera is looking.
  * Supports standard strafe walking (moving side-to-side while facing forward).
  * Toggling perspective away from Third Person Back turns off Shift Lock automatically.

* **Smooth Scroll Zoom**:
  * Use the mouse wheel to smoothly zoom in or out.
  * Safely bypasses/disables hotbar item switching while zooming.

* **Action Alignment**:
  * Attacking, placing blocks, or using items (eating, drinking, drawing bows) automatically turns the player to face the camera direction so that hitboxes align correctly.

## Compilation

To compile the mod from source, ensure you have Java 21 installed, then run:

```bash
./gradlew clean build
```

The compiled `.jar` file will be located in `build/libs/`.

## License

This mod is available under the [CC0-1.0](LICENSE) license.
