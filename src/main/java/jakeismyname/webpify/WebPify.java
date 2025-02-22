package jakeismyname.webpify;

import jakeismyname.webpify.mixin.NativeImageAccessor;
import net.minecraft.client.texture.NativeImage;
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

	private static BufferedImage toBufferedImage(NativeImage image) {
		final NativeImageAccessor imageAccessor = (NativeImageAccessor)(Object)image;
		ByteBuffer imageData = MemoryUtil.memByteBuffer(imageAccessor.getPointer(), (int) imageAccessor.getDataSize());
		return toBufferedImage(imageData, image.getWidth(), image.getHeight(), image.getFormat().getChannelCount(), true);
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
		WEBP_WRITER.setOutput(new FileImageOutputStream(file));
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