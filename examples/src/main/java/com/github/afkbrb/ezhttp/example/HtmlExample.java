package com.github.afkbrb.ezhttp.example;

import com.github.afkbrb.ezhttp.core.Server;
import com.github.afkbrb.ezhttp.core.request.Request;
import com.github.afkbrb.ezhttp.core.response.HtmlResponse;
import com.github.afkbrb.ezhttp.core.response.Response;

public class HtmlExample extends Server {

    @Override
    protected Response handle(Request request) {
        String html = "<h1 style=\"text-align: center; margin-top: 32vh; font-size: 4rem\">⎛⎝≥⏝⏝≤⎠⎞This is html example⎛⎝｡◕⏝⏝◕｡⎠⎞</h1>";
        return new HtmlResponse(html);
    }

    public static void main(String[] args) {
        new HtmlExample().start();
    }
}
