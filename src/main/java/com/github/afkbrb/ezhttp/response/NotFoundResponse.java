package com.github.afkbrb.ezhttp.response;

import com.github.afkbrb.ezhttp.protocol.HttpStatus;

public class NotFoundResponse extends FileResponse{

    public NotFoundResponse() {
        super(ClassLoader.getSystemClassLoader().getResource("page/404.html").getPath());
        statusLine.setStatus(HttpStatus.NOT_FOUND_404);
    }

}
