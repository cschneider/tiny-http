package net.lr.tinyhttp.impl;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class MimeTypes {
    private Properties typeMap;

    public MimeTypes() {
        try {
            typeMap = new Properties();
            typeMap.load(this.getClass().getResourceAsStream("/mimetypes.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    String get(File file) {
        String extension = getExtension(file.getAbsolutePath());
        return typeMap.getProperty(extension, "text/plain");
    }
    
    private String getExtension(String filename) {
        int index = indexOfExtension(filename);
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }
    
    private int indexOfExtension(String filename) {
        int ext = filename.lastIndexOf(".");
        int sep = filename.lastIndexOf("/");
        return (sep > ext ? -1 : ext);
    }
    
}
