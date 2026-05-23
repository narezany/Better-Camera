package nrz.bettercam.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import nrz.bettercam.ModConfig;

public class MultiplayerWarningScreen extends Screen {
    private final Screen parent;

    public MultiplayerWarningScreen(Screen parent) {
        super(Text.translatable("screen.better_camera.warning.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Button 1: Accept & Keep enabled
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("screen.better_camera.warning.accept"),
            button -> {
                ModConfig.INSTANCE.hasAcceptedMultiplayerWarning = true;
                ModConfig.save();
                this.client.setScreen(new MultiplayerScreen(this.parent));
            }
        ).dimensions(centerX - 150, centerY + 20, 300, 20).build());

        // Button 2: Disable Ctrl Lock
        this.addDrawableChild(ButtonWidget.builder(
            Text.translatable("screen.better_camera.warning.disable"),
            button -> {
                ModConfig.INSTANCE.hasAcceptedMultiplayerWarning = true;
                ModConfig.INSTANCE.enableCtrlLock = false;
                CameraState.shiftLock = false;
                ModConfig.save();
                this.client.setScreen(new MultiplayerScreen(this.parent));
            }
        ).dimensions(centerX - 150, centerY + 50, 300, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 60, 0xFFFF5555);
        
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.translatable("screen.better_camera.warning.line1"), 
            this.width / 2, this.height / 2 - 20, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, 
            Text.translatable("screen.better_camera.warning.line2"), 
            this.width / 2, this.height / 2, 0xFFFFFFFF);
    }
}
