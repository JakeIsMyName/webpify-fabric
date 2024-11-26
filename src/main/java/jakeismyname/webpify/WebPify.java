package jakeismyname.webpify;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import jakeismyname.webpify.mixin.NativeImageAccessor;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.NativeImage;
import org.apache.commons.io.FilenameUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import xyz.pary.webp.WebPFormat;
import xyz.pary.webp.imageio.WebPImageWriteParam;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class WebPify {
	private static final ImageWriter WEBP_WRITER;
	private static final WebPImageWriteParam WEBP_WRITE_PARAM;

	public static BufferedImage takeScreenshot(Framebuffer framebuffer) {
		final int WIDTH = framebuffer.textureWidth;
		final int HEIGHT = framebuffer.textureHeight;

		final long size = (long) WIDTH * HEIGHT * 3;
		final long bufPointer = MemoryUtil.nmemAlloc(size);

		RenderSystem.bindTexture(framebuffer.getColorAttachment());
		GlStateManager._pixelStore(GL11.GL_PACK_ALIGNMENT, 1);
		GlStateManager._getTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, bufPointer);
		ByteBuffer imageData = MemoryUtil.memByteBuffer(bufPointer, (int) size);

		return toBufferedImage(imageData, WIDTH, HEIGHT, 3, false);
	}

	private static BufferedImage toBufferedImage(NativeImage image) {
		final int WIDTH = image.getWidth();
		final int HEIGHT = image.getHeight();
		final NativeImageAccessor imageAccessor = (NativeImageAccessor)(Object)image;
		ByteBuffer imageData = MemoryUtil.memByteBuffer(imageAccessor.getPointer(), (int) imageAccessor.getDataSize());
		return toBufferedImage(imageData, WIDTH, HEIGHT, image.getFormat().getChannelCount(), true);
	}

	private static BufferedImage toBufferedImage(ByteBuffer imageData, int width, int height, int channels, boolean flip) {
		final BufferedImage bufImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final int rowSize = width * channels;
		for (int y = 0; y < height; y++) {
			int rowStart = (height - 1 - y) * rowSize;
			for (int x = 0; x < width; x++) {
				int offset = rowStart + x * channels;
				int r = (imageData.get(offset) & 0xFF) << 16;
				int g = (imageData.get(offset + 1) & 0xFF) << 8;
				int b = (imageData.get(offset + 2) & 0xFF);
				bufImage.setRGB(x, flip ? (height - 1) - y : y, r | g | b);
			}
		}

		return bufImage;
	}

	public static void saveWebpImage(NativeImage image, File file) throws IOException {
		saveWebpImage(toBufferedImage(image), file);
	}

	public static void saveWebpImage(BufferedImage image, File file) throws IOException {
		WEBP_WRITER.setOutput(new FileImageOutputStream(new File(FilenameUtils.removeExtension(file.toString()) + ".webp")));
		WEBP_WRITER.write(null, new IIOImage(image, null, null), WEBP_WRITE_PARAM);
		WEBP_WRITER.reset();
	}

	static {
		WEBP_WRITER = ImageIO.getImageWritersByMIMEType("image/webp").next();
		WEBP_WRITE_PARAM = new WebPImageWriteParam(null);
		WEBP_WRITE_PARAM.setCompressionType(WebPFormat.LOSSLESS);
		WEBP_WRITE_PARAM.setCompressionQuality(0.0f);
	}

}