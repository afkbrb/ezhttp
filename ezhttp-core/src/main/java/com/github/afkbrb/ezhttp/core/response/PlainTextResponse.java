package com.github.afkbrb.ezhttp.core.response;

import com.github.afkbrb.ezhttp.core.conf.Config;

public class PlainTextResponse extends Response{

    public PlainTextResponse(String txt) {
        headers.put("Content-Type", "text/plain; charset=" + Config.CHARSET);
        if (txt == null) return;
        body = txt.getBytes();
    }
}
