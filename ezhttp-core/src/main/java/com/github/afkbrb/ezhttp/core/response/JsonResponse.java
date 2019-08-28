package com.github.afkbrb.ezhttp.core.response;

import com.alibaba.fastjson.JSON;
import com.github.afkbrb.ezhttp.core.conf.Config;
import com.github.afkbrb.ezhttp.core.protocol.HttpStatus;

import java.io.UnsupportedEncodingException;

public class JsonResponse extends Response{

    public JsonResponse(Object object) {
        headers.put("Content-Type", "application/json; charset=" + Config.CHARSET);
        try {
            body = JSON.toJSONString(object).getBytes(Config.CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            statusLine.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
        }
    }
}
