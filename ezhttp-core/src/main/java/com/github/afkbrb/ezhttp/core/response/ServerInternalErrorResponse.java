package com.github.afkbrb.ezhttp.core.response;

import com.github.afkbrb.ezhttp.core.protocol.HttpStatus;

public class ServerInternalErrorResponse extends FileResponse{

    public ServerInternalErrorResponse() {
        super(ClassLoader.getSystemClassLoader().getResource("page/500.html").getPath());
        statusLine.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
    }
}
