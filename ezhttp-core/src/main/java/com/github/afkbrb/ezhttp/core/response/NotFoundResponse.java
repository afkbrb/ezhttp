package com.github.afkbrb.ezhttp.core.response;

import com.github.afkbrb.ezhttp.core.protocol.HttpStatus;

public class NotFoundResponse extends FileResponse{

    public NotFoundResponse() {
        super(ClassLoader.getSystemClassLoader().getResource("page/404.html").getPath());
        statusLine.setStatus(HttpStatus.NOT_FOUND_404);
    }

}
