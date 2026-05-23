package nrz.bettercam.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import nrz.bettercam.ModConfig;

public class ConfigScreen extends Screen {
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("screen.better_camera.settings.title"));
        this.parent = parent;
    }

    private Component getFPZoomText() {
        return Component.translatable("screen.better_camera.settings.first_person_zoom", 
            ModConfig.INSTANCE.enableFirstPersonZoom ? Component.translatable("screen.better_camera.settings.on") : Component.translatable("screen.better_camera.settings.off"));
    }

    private Component getTransitionsText() {
        return Component.translatable("screen.better_camera.settings.transitions", 
            ModConfig.INSTANCE.enableTransitions ? Component.translatable("screen.better_camera.settings.on") : Component.translatable("screen.better_camera.settings.off"));
    }

    private Component getCtrlLockText() {
        return Component.translatable("screen.better_camera.settings.ctrl_lock", 
            ModConfig.INSTANCE.enableCtrlLock ? Component.translatable("screen.better_camera.settings.on") : Component.translatable("screen.better_camera.settings.off"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Button 1: First Person Zoom
        this.addRenderableWidget(Button.builder(
            getFPZoomText(),
            button -> {
                ModConfig.INSTANCE.enableFirstPersonZoom = !ModConfig.INSTANCE.enableFirstPersonZoom;
                ModConfig.save();
                button.setMessage(getFPZoomText());
            }
        ).bounds(centerX - 100, centerY - 60, 200, 20).build());

        // Button 2: Transitions & Smoothness
        this.addRenderableWidget(Button.builder(
            getTransitionsText(),
            button -> {
                ModConfig.INSTANCE.enableTransitions = !ModConfig.INSTANCE.enableTransitions;
                ModConfig.save();
                button.setMessage(getTransitionsText());
            }
        ).bounds(centerX - 100, centerY - 35, 200, 20).build());

        // Button 3: Ctrl Lock
        this.addRenderableWidget(Button.builder(
            getCtrlLockText(),
            button -> {
                ModConfig.INSTANCE.enableCtrlLock = !ModConfig.INSTANCE.enableCtrlLock;
                if (!ModConfig.INSTANCE.enableCtrlLock) {
                    CameraState.shiftLock = false;
                }
                ModConfig.save();
                button.setMessage(getCtrlLockText());
            }
        ).bounds(centerX - 100, centerY - 10, 200, 20).build());

        // Slider: HUD Sway Strength
        this.addRenderableWidget(new AbstractSliderButton(
            centerX - 100, centerY + 15, 200, 20,
            Component.translatable("screen.better_camera.settings.hud_sway", (int) (ModConfig.INSTANCE.hudSwayStrength * 100)),
            ModConfig.INSTANCE.hudSwayStrength
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Component.translatable("screen.better_camera.settings.hud_sway", (int) (this.value * 100)));
            }

            @Override
            protected void applyValue() {
                ModConfig.INSTANCE.hudSwayStrength = (float) this.value;
                ModConfig.save();
            }
        });

        // Back Button
        this.addRenderableWidget(Button.builder(
            Component.translatable("screen.better_camera.settings.done"),
            button -> this.minecraft.setScreen(this.parent)
        ).bounds(centerX - 100, centerY + 45, 200, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        
        // Draw title
        context.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}
