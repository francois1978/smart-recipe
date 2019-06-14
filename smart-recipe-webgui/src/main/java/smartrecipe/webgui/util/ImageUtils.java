package smartrecipe.webgui.util;

import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtils {
    public static byte[] rotateImage(byte[] imageAsByteArray, double angle) throws IOException {

        BufferedImage img = null;
        img = ImageIO.read(new ByteArrayInputStream(imageAsByteArray));

        double sin = Math.abs(Math.sin(Math.toRadians(angle))),
                cos = Math.abs(Math.cos(Math.toRadians(angle)));

        int w = img.getWidth(null), h = img.getHeight(null);

        int neww = (int) Math.floor(w * cos + h * sin),
                newh = (int) Math.floor(h * cos + w * sin);

        BufferedImage bimg = new BufferedImage(neww, newh, img.getType());
        Graphics2D g = bimg.createGraphics();

        g.translate((neww - w) / 2, (newh - h) / 2);
        g.rotate(Math.toRadians(angle), w / 2, h / 2);
        g.drawRenderedImage(img, null);
        g.dispose();

        return toByteArray(bimg);

    }

    public static byte[] toByteArray(BufferedImage originalImage) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(originalImage, "jpg", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        return imageInByte;
    }

    //Utils for resizing image at scale
    public static byte[] geScaledImage(byte[] imageAsByteArray, int targetSize) throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream(imageAsByteArray);

        BufferedImage originalImage = ImageIO.read(bais);

        originalImage = Scalr.resize(originalImage, targetSize);

        return toByteArray(originalImage);

    }
}
