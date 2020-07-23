package nl.jads.sodalite.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ResourceUtil {
    public static File getStringAsFile(String value) {
        try {
//            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
//            if (in == null) {
//                return null;
//            }

            File tempFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".yaml");
            tempFile.deleteOnExit();
            FileUtils.writeStringToFile(tempFile, value, Charset.defaultCharset());
//            try (FileOutputStream out = new FileOutputStream(tempFile)) {
//                //copy stream
//                byte[] buffer = new byte[1024];
//                int bytesRead;
//                while ((bytesRead = in.read(buffer)) != -1) {
//                    out.write(buffer, 0, bytesRead);
//                }
//            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
