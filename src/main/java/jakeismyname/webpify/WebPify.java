package jakeismyname.webpify;

import com.luciad.imageio.webp.WebPWriteParam;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class WebPify {
	public static DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss", Locale.ROOT);

	public static BufferedImage takeScreenshot(Framebuffer framebuffer) {
		int WIDTH = framebuffer.textureWidth;
		int HEIGHT = framebuffer.textureHeight;

		long size = (long) WIDTH * HEIGHT * 3;
		long bufPointer = MemoryUtil.nmemAlloc(size);

		RenderSystem.bindTexture(framebuffer.getColorAttachment());
		GlStateManager._pixelStore(GL11.GL_PACK_ALIGNMENT, 1);
		GlStateManager._getTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, bufPointer);
		ByteBuffer imageData = MemoryUtil.memByteBuffer(bufPointer, (int)size);

		int[] rgbArray = new int[WIDTH * HEIGHT];
		int rowSize = WIDTH * 3;
		for (int y = 0; y < HEIGHT; y++) {
			int rowStart = (HEIGHT - 1 - y) * rowSize;
			int arrayStart = y * WIDTH;
			for (int x = 0; x < WIDTH; x++) {
				int offset = rowStart + x * 3;
				int r = (imageData.get(offset) & 0xFF) << 16;
				int g = (imageData.get(offset + 1) & 0xFF) << 8;
				int b = (imageData.get(offset + 2) & 0xFF);
				rgbArray[arrayStart + x] = r | g | b;
			}
		}

		BufferedImage bufImage = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		bufImage.setRGB(0, 0, WIDTH, HEIGHT, rgbArray, 0, WIDTH);

		return bufImage;
	}

	public static File getWebpScreenshotFilename(File dir) {
		String baseName = DT_FORMATTER.format(ZonedDateTime.now());
		int i = 1;
		while (true) {
			File file = new File(dir, baseName + (i > 1 ? i : "") + ".webp");
			if (!file.exists()) return file;
			else i++;
		}
	}

	public static void saveWebpImage(BufferedImage image, File file) throws IOException {
		ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
		WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());

		writeParam.setCompressionType(writeParam.getCompressionTypes()[WebPWriteParam.LOSSLESS_COMPRESSION]);
		writeParam.setCompressionQuality(1.0f);

		writer.setOutput(new FileImageOutputStream(file));

		writer.write(null, new IIOImage(image, null, null), writeParam);
		writer.dispose();
	}

}