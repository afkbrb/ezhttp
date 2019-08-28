package com.github.afkbrb.ezhttp.example;

import com.github.afkbrb.ezhttp.core.Server;
import com.github.afkbrb.ezhttp.core.request.Request;
import com.github.afkbrb.ezhttp.core.response.FileResponse;
import com.github.afkbrb.ezhttp.core.response.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ImageExample extends Server {

    @Override
    protected Response handle(Request request) {
        // If there are no special characters in the resource path, you don't need to decode it.
        // But always decode it to make sure it works without surprise.
        String urlPath = ClassLoader.getSystemClassLoader().getResource("wick rick.jpg").getFile();
        String path = null;
        try {
            path = URLDecoder.decode(urlPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("path: " + path);
        return new FileResponse(path);
    }

    public static void main(String[] args) {
        Server server = new ImageExample();
        server.start();
    }
}
