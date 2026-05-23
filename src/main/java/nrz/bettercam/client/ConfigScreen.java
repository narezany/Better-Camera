package nrz.bettercam.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nrz.bettercam.ModConfig;

public class ConfigScreen extends Screen {
    private final Screen parent;

    public ConfigScreen(Screen parent) {
        super(Text.translatable("screen.better_camera.settings.title"));
        this.parent = parent;
    }

    private Text getFPZoomText() {
        return Text.translatable("screen.better_camera.settings.first_person_zoom", 
            ModConfig.INSTANCE.enableFirstPersonZoom ? Text.translatable("screen.better_camera.settings.on") : Text.translatable("screen.better_camera.settings.off"));
    }

    private Text getTransitionsText() {
        return Text.translatable("screen.better_camera.settings.transitions", 
            ModConfig.INSTANCE.enableTransitions ? Text.translatable("screen.better_camera.settings.on") : Text.translatable("screen.better_camera.settings.off"));
    }

    private Text getCtrlLockText() {
        return Text.translatable("screen.better_camera.settings.ctrl_lock", 
            ModConfig.INSTANCE.enableCtrlLock ? Text.translatable("screen.better_camera.settings.on") : Text.translatable("screen.better_camera.settings.off"));
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Button 1: First Person Zoom
        this.addDrawableChild(ButtonWidget.builder(
            getFPZoomText(),
            button -> {
                ModConfig.INSTANCE.enableFirstPersonZoom = !ModConfig.INSTANCE.enableFirstPersonZoom;
                ModConfig.save();
                button.setMessage(getFPZoomText());
            }
        ).dimensions(centerX - 100, centerY - 60, 200, 20).build());

        // Button 2: Transitions & Smoothness
        this.addDrawableChild(ButtonWidget.builder(
            getTransitionsText(),
            button -> {
                ModConfig.INSTANCE.enableTransitions = !ModConfig.INSTANCE.enableTransitions;
                ModConfig.save();
                button.setMessage(getTransitionsText());
            }
        ).dimensions(centerX - 100, centerY - 35, 200, 20).build());

        // Button 3: Ctrl Lock
        this.addDrawableChild(ButtonWidget.builder(
            getCtrlLockText(),
            button -> {
                ModConfig.INSTANCE.enableCtrlLock = !ModConfig.INSTANCE.enableCtrlLock;
                if (!ModConfig.INSTANCE.enableCtrlLock) {
                    CameraState.shiftLock = false;
                }
                ModConfig.save();
                button.setMessage(getCtrlLockText());
            }
        ).dimensions(centerX - 100, centerY - 10, 200, 20).build());

        // Slider: HUD Sway Strength
        this.addDrawableChild(new net.minecraft.client.gui.widget.SliderWidget(
            centerX - 100, centerY + 15, 200, 20,
            Text.translatable("screen.better_camera.settings.hud_sway", (int) (ModConfig.INSTANCE.hudSwayStrength * 100)),
            ModConfig.INSTANCE.hudSwayStrength
        ) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.translatable("screen.better_camera.settings.hud_sway", (int) (this.value * 100)));
            }

            @Override
            protected void applyValue() {
                ModConfig.INSTANCE.hudSwayStrength = (float) this.value;
                ModConfig.save();
            }
        });

        // Back Button
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("screen.better_camera.settings.done"),
            button -> this.client.setScreen(this.parent)
        ).dimensions(centerX - 100, centerY + 45, 200, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        // Draw title
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 20, 0xFFFFFF);
    }
}
