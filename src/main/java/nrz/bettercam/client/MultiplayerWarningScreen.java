package nrz.bettercam.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import nrz.bettercam.ModConfig;

public class MultiplayerWarningScreen extends Screen {
    private final Screen parent;

    public MultiplayerWarningScreen(Screen parent) {
        super(Component.translatable("screen.better_camera.warning.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Button 1: Accept & Keep enabled
        this.addRenderableWidget(Button.builder(
            Component.translatable("screen.better_camera.warning.accept"),
            button -> {
                ModConfig.INSTANCE.hasAcceptedMultiplayerWarning = true;
                ModConfig.save();
                this.minecraft.setScreen(new JoinMultiplayerScreen(this.parent));
            }
        ).bounds(centerX - 150, centerY + 20, 300, 20).build());

        // Button 2: Disable Ctrl Lock
        this.addRenderableWidget(Button.builder(
            Component.translatable("screen.better_camera.warning.disable"),
            button -> {
                ModConfig.INSTANCE.hasAcceptedMultiplayerWarning = true;
                ModConfig.INSTANCE.enableCtrlLock = false;
                CameraState.shiftLock = false;
                ModConfig.save();
                this.minecraft.setScreen(new JoinMultiplayerScreen(this.parent));
            }
        ).bounds(centerX - 150, centerY + 50, 300, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        
        context.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 60, 0xFFFF5555);
        
        context.centeredText(this.font, 
            Component.translatable("screen.better_camera.warning.line1"), 
            this.width / 2, this.height / 2 - 20, 0xFFFFFFFF);
        context.centeredText(this.font, 
            Component.translatable("screen.better_camera.warning.line2"), 
            this.width / 2, this.height / 2, 0xFFFFFFFF);
    }
}
