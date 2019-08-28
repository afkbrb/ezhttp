package com.github.afkbrb.ezhttp.response;

import com.github.afkbrb.ezhttp.conf.Config;
import com.github.afkbrb.ezhttp.protocol.HttpStatus;

import java.io.UnsupportedEncodingException;

public class HtmlResponse extends Response{

    public HtmlResponse(String html) {
        super(HttpStatus.OK_200);
        headers.put("Content-Type", "text/html; charset=" + Config.CHARSET);
        buildBody(html);
    }

    private void buildBody(String html) {
        if (html == null) return;
        try {
            byte[] bytes = html.getBytes(Config.CHARSET);
            body = bytes;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
