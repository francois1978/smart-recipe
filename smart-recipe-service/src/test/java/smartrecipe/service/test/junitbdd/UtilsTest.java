package smartrecipe.service.test.junitbdd;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import smartrecipe.service.SrServicesApplication;
import smartrecipe.service.test.servicemock.GoogleOcrServiceMockConfiguration;
import smartrecipe.service.utils.Hash;
import smartrecipe.service.utils.ImageUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SrServicesApplication.class)
public class UtilsTest {
  

    @Test
    public void given2Images_whenMergeAndRotate_thenMergeResultOk() throws IOException {
        List<byte[]> imageList = getImageList();
        byte[] imageMerged = ImageUtils.mergeImagesList(imageList,true, 90);
        assertTrue(Hash.MD5.checksum(imageMerged).equalsIgnoreCase("0092D79AAED64368B260E8374B42408C"));
    }

    @Test
    public void given2Images_whenMerge_thenMergeResultOk() throws IOException {
        List<byte[]> imageList = getImageList();
        byte[] imageMerged = ImageUtils.mergeImagesList(imageList,true, 0);
        assertTrue(Hash.MD5.checksum(imageMerged).equalsIgnoreCase("0092D79AAED64368B260E8374B42408C"));
    }

    @Test
    public void givenImage_whenRotate_thenRotatedResultOk() throws IOException {
        byte[] image = GoogleOcrServiceMockConfiguration.getRecipeBinaryEntity().getBinaryDescription();
        byte[] imageRotated = ImageUtils.rotateImage(image, -90);
        assertTrue(Hash.MD5.checksum(imageRotated).equalsIgnoreCase("F70B8576527AC5287B7D06D069F22B96"));
    }

    private List<byte[]> getImageList() throws IOException {
        byte[] image1 = GoogleOcrServiceMockConfiguration.getRecipeBinaryEntity().getBinaryDescription();
        byte[] image2 = Arrays.copyOfRange(image1, 0, image1.length);
        List<byte[]> imageList = new ArrayList();
        imageList.add(image1);
        imageList.add(image2);
        return imageList;
    }

}