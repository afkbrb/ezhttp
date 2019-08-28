package com.github.afkbrb.ezhttp.response;

import com.github.afkbrb.ezhttp.protocol.HttpStatus;

public class ServerInternalErrorResponse extends FileResponse{

    public ServerInternalErrorResponse() {
        super(ClassLoader.getSystemClassLoader().getResource("page/500.html").getPath());
        statusLine.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }
}
