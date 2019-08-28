package com.github.afkbrb.ezhttp.example;

import com.github.afkbrb.ezhttp.core.Server;
import com.github.afkbrb.ezhttp.core.request.Request;
import com.github.afkbrb.ezhttp.core.response.JsonResponse;
import com.github.afkbrb.ezhttp.core.response.Response;

import java.util.HashMap;
import java.util.Map;

public class JsonExample extends Server {

    @Override
    protected Response handle(Request request) {
        Map map = new HashMap();
        map.put("key", "item");
        map.put("list", new String[] {"list-item1", "list-item2", "list-item3"});
        Map map1 = new HashMap();
        map1.put("inner-key", "inner-item");
        map.put("map", map1);
        return new JsonResponse(map);
    }

    public static void main(String[] args) {
        new JsonExample().start();
    }
}
