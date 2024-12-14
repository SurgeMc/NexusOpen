package net.cubzyn;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.client.gui.DrawContext;

public class CustomMainMenuScreen extends TitleScreen {

    private ButtonWidget exampleButton;

    @Override
    protected void init() {
        super.init();

        // Create the button using the builder pattern
        this.exampleButton = ButtonWidget.builder(
                        Text.of("Click Me!"),
                        button -> {
                            // Action when the button is clicked
                            MinecraftClient.getInstance().player.sendMessage(Text.of("Button clicked!"), false);
                        })
                .dimensions(this.width / 2 - 100, this.height / 2 + 24, 200, 20)
                .build();

        // Add the button to the screen using addDrawableChild
        this.addDrawableChild(exampleButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);  // Updated call with correct arguments

        super.render(context, mouseX, mouseY, delta);  // Render other elements (like the logo)

        // Draw the custom button
        this.exampleButton.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void tick() {
        super.tick();
    }
}
