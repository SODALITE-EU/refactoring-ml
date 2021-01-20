package nl.jads.sodalite.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class ResourceUtil {
    private static final java.util.logging.Logger log = Logger.getLogger(ResourceUtil.class.getName());

    public static File getStringAsFile(String value) {
        try {
            File tempFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".yaml");
            tempFile.deleteOnExit();
            FileUtils.writeStringToFile(tempFile, value, Charset.defaultCharset());
            return tempFile;
        } catch (IOException e) {
            log.warning(e.getMessage());
            return null;
        }
    }
}
