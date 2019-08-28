package com.github.afkbrb.ezhttp.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MimeUtil {

    private static final Map<String, String> mimeMap = new HashMap<>();

    static {
        try {
            Properties properties = new Properties();
            properties.load(ClassLoader.getSystemClassLoader().getResourceAsStream("mime.properties"));
            for (Object key : properties.keySet()) {
                mimeMap.put((String) key, (String) properties.get(key));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Decide the content type by path
     * @param path
     * @return
     */
    public static String getContentType(String path){
        int i = path.lastIndexOf(".");
        if(i == -1 || i == path.length() - 1) return "text/plain";
        String suffix = path.substring(i+1);
        return mimeMap.getOrDefault(suffix,"text/plain");
    }
}
