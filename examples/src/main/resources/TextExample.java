/**
 * This file is from the resources folder!
 */

package com.github.afkbrb.ezhttp.example;

import com.github.afkbrb.ezhttp.core.Server;
import com.github.afkbrb.ezhttp.core.request.Request;
import com.github.afkbrb.ezhttp.core.response.FileResponse;
import com.github.afkbrb.ezhttp.core.response.Response;

public class TextExample extends Server {

    @Override
    protected Response handle(Request request) {
        // Since the resource path does not have special characters, we don't need to decode it which is different from what we write in ImageExample
        String path = ClassLoader.getSystemClassLoader().getResource("TextExample.java").getFile();
        return new FileResponse(path);
    }

    public static void main(String[] args) {
        new TextExample().start();
    }
}
