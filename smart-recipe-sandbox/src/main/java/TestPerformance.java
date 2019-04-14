import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;

@Slf4j
public class TestPerformance {


    public static void main(String args[]) {

        byte[] image = null;
        try {
            image = Files.readAllBytes(new File("C:\\dev\\temp\\IMG_0015.jpg").toPath());

            long start = System.currentTimeMillis();
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(image));

            ByteArrayOutputStream os = new ByteArrayOutputStream();

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            ImageWriter writer = writers.next();

            ImageOutputStream ios = ImageIO.createImageOutputStream(os);
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();

            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.05f);  // Change the quality value you prefer
            writer.write(null, new IIOImage(bufferedImage, null, null), param);

            os.close();
            ios.close();
            writer.dispose();

            long end = System.currentTimeMillis();

            log.info("Compression duration " + (end - start));

            log.info("Source byte size: " + image.length);
            log.info("Target byte size: " + os.toByteArray().length);


            //FileUtils.writeByteArrayToFile(new File("C:\\dev\\temp\\" + System.currentTimeMillis() + ".jpg"), os.toByteArray());


        } catch (IOException e) {
            log.error("Error while creating a Recipe with binary image from disk", e);
        }
    }
}
