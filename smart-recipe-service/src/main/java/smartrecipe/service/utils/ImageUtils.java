package smartrecipe.service.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.stream.Collectors;

@Slf4j
public class ImageUtils {


    private static byte[] getImageAsByteFromBufferedImage(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();
        return imageInByte;
    }

    public static byte[] mergeImagesList(java.util.List<byte[]> imageList, boolean rotate, double angle) throws IOException {

        java.util.List<byte[]> rotatedImages = imageList.stream().map(e -> {
            try {

                BufferedImage img = ImageIO.read(new ByteArrayInputStream(e));
                if (img.getWidth() > img.getHeight()) {
                    return rotate ? rotateImage(e, angle) : e;
                }
            } catch (IOException e1) {
                log.error(e1.getMessage(), e1);
            }
            return e;
        }).collect(Collectors.toList());

        byte[] previousMergedImage = rotatedImages.get(0);
        BufferedImage referenceImageSize = ImageIO.read(new ByteArrayInputStream(rotatedImages.get(0)));

        for (int i = 1; i < rotatedImages.size(); i++) {
            //byte[] currentImage = rotate ? rotateImage(rotatedImages.get(i), angle) : rotatedImages.get(i);
            byte[] resizedImage = resize(rotatedImages.get(i),referenceImageSize.getWidth(), referenceImageSize.getHeight());
            previousMergedImage = mergeImages(previousMergedImage, resizedImage);
        }

        return previousMergedImage;
    }

    public static byte[] resize(byte[] imgAsByteArray, int newW, int newH) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imgAsByteArray));
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return toByteArray(dimg);
    }

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


    public static byte[] mergeImages(byte[] image1, byte[] image2) throws IOException {

        InputStream input1 = new ByteArrayInputStream(image1);
        BufferedImage imageBuffer1 = ImageIO.read(input1);

        InputStream input2 = new ByteArrayInputStream(image2);
        BufferedImage imageBuffer2 = ImageIO.read(input2);

        int widthTotal = imageBuffer1.getWidth() + imageBuffer2.getWidth();

        BufferedImage concatImage =
                new BufferedImage(widthTotal, Math.max(imageBuffer1.getHeight(), imageBuffer2.getHeight()), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = concatImage.createGraphics();

        g2d.drawImage(imageBuffer1, 0, 0, null);
        g2d.drawImage(imageBuffer2, imageBuffer1.getWidth(), 0, null);

        g2d.dispose();

        ImageIO.write(concatImage, "jpg", new File("C:\\dev\\temp\\recipes_import\\merged.jpg"));
        return getImageAsByteFromBufferedImage(concatImage);
    }

    /*
        Compress image to build light recipe
         */
    public static byte[] compressByteArray(byte[] inputByteArray) throws IOException {
        long start = System.currentTimeMillis();
        //build
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(inputByteArray));

        //setup output
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter writer = writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);


        //setup compression
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(0.05f);  // Change the quality value you prefer

        //compress byte[] image
        writer.write(null, new IIOImage(bufferedImage, null, null), param);

        os.close();
        ios.close();
        writer.dispose();

        long end = System.currentTimeMillis();

        log.info("Compression duration " + (end - start));
        log.info("Source byte size: " + inputByteArray.length);
        log.info("Target byte size: " + os.toByteArray().length);


        //FileUtils.writeByteArrayToFile(new File("C:\\dev\\temp\\" + System.currentTimeMillis() + ".jpg"), os.toByteArray());

        return os.toByteArray();
    }
}
