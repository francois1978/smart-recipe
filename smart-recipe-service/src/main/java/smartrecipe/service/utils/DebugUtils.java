package smartrecipe.service.utils;

import org.apache.commons.io.IOUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class DebugUtils {

    public static void log(InputStream is){
        StringWriter writer = new StringWriter();
        try {
            IOUtil.copy(is, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String theString = writer.toString();
        System.out.println(theString);
    }
}
