package smartrecipe.service.helper;

public interface GoogleOCRDetectionService {
    String getTextFromImage(byte[] image, boolean ocrInTestMode) throws Exception;
}
