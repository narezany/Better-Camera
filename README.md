# Better Camera - Roblox Camera & Shift Lock for Minecraft

A Minecraft Fabric mod designed to overhaul the third-person camera perspective to function similarly to Roblox, complete with an over-the-shoulder Shift Lock mode, custom crosshair rendering, and smooth scroll zoom.

## Features

1. **Roblox Orbit Camera**:
   - Skips standard front-view perspective; cycles only between **First Person** and **Third Person Back** using the standard F5 key.
   - Mouse movement rotates the camera *around* the player character without turning the player.
   - The player character only turns to face the movement direction when walking (using WASD).

2. **Roblox Shift Lock (L-CTRL Toggle)**:
   - Press **Left Control** in third person to toggle **Shift Lock**.
   - Camera offsets slightly **up** and **to the right** (over-the-shoulder view).
   - A custom **round crosshair** renders dynamically in the center of the screen.
   - The player character constantly and smoothly rotates to face the exact direction the camera is looking.
   - Standard strafe walking is supported (moving side-to-side while facing forward).
   - Toggling perspective away from Third Person Back turns off Shift Lock automatically.

3. **Smooth Scroll Zoom**:
   - Mouse wheel scrolls to smoothly zoom in or out (radius between 1.5 and 15 blocks).
   - Hotbar item switching is safely bypassed/disabled while zooming.

4. **Action Alignment**:
   - Attacking, placing blocks, or using items (eating, drinking, drawing bows) automatically turns the player to face the camera direction so hitboxes align correctly.

---

## Mod Publishing & Source Code Guide

### 1. Uploading the Source Code to GitHub

Since this mod is fully local on your machine, here are the simple commands to push it to a new GitHub repository:

1. Open your terminal in the mod folder:
   ```bash
   cd ~/Projects/better-camera
   ```
2. Initialize git and make a local commit:
   ```bash
   git init
   git add .
   git commit -m "Initial commit: Roblox camera system with Shift Lock"
   ```
3. Open your browser and go to [GitHub New Repository](https://github.com/new).
   - Set the Repository name to `better-camera-roblox`.
   - Leave it public, do **not** add a README, `.gitignore`, or LICENSE (since they already exist in this folder).
   - Click **Create repository**.
4. Link your local project to GitHub and push:
   ```bash
   git branch -M main
   git remote add origin https://github.com/YOUR_GITHUB_USERNAME/better-camera-roblox.git
   git push -u origin main
   ```
   *(Replace `YOUR_GITHUB_USERNAME` with your actual GitHub username).*

---

## 2. Uploading the Mod to Modrinth

To share your mod with the world on Modrinth:

1. **Build the JAR**:
   Run the build command in the mod directory:
   ```bash
   ./gradlew clean build
   ```
   The compiled mod file is generated at: `build/libs/better-camera-1.0.0.jar`.

2. **Submit to Modrinth**:
   - Go to [Modrinth](https://modrinth.com/) and log in/sign up.
   - Click on **Create** or go to [Modrinth Create Project](https://modrinth.com/create).
   - Choose **Mod** as the project type.
   - Fill out the details:
     - **Title**: `Better Camera` or `Roblox Camera & Shift Lock`
     - **Description**: Add a description highlighting the orbit camera, L-CTRL Shift Lock, and smooth scroll zoom.
     - **Category**: `Utility`, `Client`
     - **License**: CC0 (configured in template)
     - **Source Code Link**: Paste your new GitHub repository URL (`https://github.com/YOUR_GITHUB_USERNAME/better-camera-roblox`).
   - Create the project, and then click **Publish Version**:
     - Upload your compiled JAR file (`better-camera-1.0.0.jar`).
     - Select **Fabric** as the loader.
     - Select **1.21.1** and **1.21.11** as the target game versions.
     - Submit, and your mod will be live after a quick approval check!

---

## License

This mod is available under the CC0-1.0 license. Feel free to modify and adapt it for your own setups.
