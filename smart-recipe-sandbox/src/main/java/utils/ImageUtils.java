package utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.queryparser.classic.ParseException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@Slf4j
public class ImageUtils {

    public static void main2(String args[]) throws IOException, ParseException {

        byte[] image1 = getImageAsByte("C:\\dev\\temp\\recipes_import_dev\\IMG_0017.jpg");
        byte[] image2 = getImageAsByte("C:\\dev\\temp\\recipes_import_dev\\IMG_0018.jpg");

        //byte[] imageMerged = mergeImages(image1, image2);
        log.info("MD5 checksum: " + Hash.MD5.checksum(image1));
    }

    private static byte[] getImageAsByte(String filePath) {

        byte[] image = null;
        try {
            image = FileUtils.readFileToByteArray(new File(filePath));
        } catch (IOException e) {
            log.error("Error while creating a Recipe with binary image from disk", e);
        }
        return image;

    }

    private static byte[] getImageAsByteFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        return imageInByte;
    }

    public static byte[] mergeImages(byte[] image1, byte[] image2) throws IOException {

        InputStream input1 = new ByteArrayInputStream(image1);
        BufferedImage imageBuffer1 = ImageIO.read(input1);

        InputStream input2 = new ByteArrayInputStream(image2);
        BufferedImage imageBuffer2 = ImageIO.read(input2);

        int widthTotal = imageBuffer1.getWidth() + imageBuffer2.getWidth();

        BufferedImage concatImage =
                new BufferedImage(widthTotal,Math.max(imageBuffer1.getHeight(), imageBuffer2.getHeight()), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = concatImage.createGraphics();

        g2d.drawImage(imageBuffer1, 0, 0, null);
        g2d.drawImage(imageBuffer1, imageBuffer1.getWidth(), 0, null);

        g2d.dispose();

        ImageIO.write(concatImage, "jpg", new File("C:\\dev\\temp\\recipes_import\\merged.jpg"));
        return getImageAsByteFromBufferedImage(concatImage);
    }
}
