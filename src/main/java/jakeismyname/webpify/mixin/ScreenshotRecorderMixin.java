package jakeismyname.webpify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import jakeismyname.webpify.WebPify;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import org.apache.commons.io.FilenameUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.io.File;
import java.io.IOException;

@Mixin(ScreenshotRecorder.class)
public abstract class ScreenshotRecorderMixin {
	@WrapOperation(method = "method_1661", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;writeTo(Ljava/io/File;)V"))
	private static void webpify$writeWebP(NativeImage image, File path, Operation<Void> original) throws IOException {
		if (FilenameUtils.getExtension(path.toString()).equals("webp")) {
			WebPify.saveWebpImage(image, path);
		} else {
			original.call(image, path);
		}
	}

	@ModifyArg(method = "getScreenshotFilename", at = @At(value = "INVOKE", target = "Ljava/io/File;<init>(Ljava/io/File;Ljava/lang/String;)V"), index = 1)
	private static String webpify$WebPFilename(String file) {
		return file.replace("png", "webp");
	}

}
