package smartrecipe.service.helper;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

@Slf4j
public class RecipeHelper {

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
