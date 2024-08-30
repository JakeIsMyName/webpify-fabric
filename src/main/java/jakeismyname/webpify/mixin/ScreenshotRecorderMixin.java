package jakeismyname.webpify.mixin;

import jakeismyname.webpify.WebPify;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public abstract class ScreenshotRecorderMixin {

    @Shadow @Final private static Logger LOGGER;

    /**
     * @author JakeIsMyName
     * @reason Save WebP screenshots instead of PNG
     */
    @Overwrite
    private static void saveScreenshotInner(File gameDirectory, @Nullable String fileName, Framebuffer framebuffer, Consumer<Text> messageReceiver) throws IOException {
        BufferedImage screenshot = WebPify.takeScreenshot(framebuffer);
        File screenshotsDir = new File(gameDirectory, "screenshots");
        screenshotsDir.mkdir();

        File output;
        if (fileName != null) output = new File(screenshotsDir, fileName);
        else output = WebPify.getWebpScreenshotFilename(screenshotsDir);

        Util.getIoWorkerExecutor().execute(() -> {
            try {
                WebPify.saveWebpImage(screenshot, output);
                Text text = Text.literal(output.getName()).formatted(Formatting.UNDERLINE).styled((style) ->
                        style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, output.getAbsolutePath()))
                );
                messageReceiver.accept(Text.translatable("screenshot.success", text));
            } catch (Exception e) {
                LOGGER.warn("Failed to save screenshot", e);
                messageReceiver.accept(Text.translatable("screenshot.failure", e.getMessage()));
            }
        });

    }

}
