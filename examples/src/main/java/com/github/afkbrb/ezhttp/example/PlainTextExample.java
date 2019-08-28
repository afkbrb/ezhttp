package com.github.afkbrb.ezhttp.example;

import com.github.afkbrb.ezhttp.core.Server;
import com.github.afkbrb.ezhttp.core.request.Request;
import com.github.afkbrb.ezhttp.core.response.PlainTextResponse;
import com.github.afkbrb.ezhttp.core.response.Response;

public class PlainTextExample extends Server {

    @Override
    protected Response handle(Request request) {
        String txt = "<h1 style=\"text-align: center; margin-top: 32vh; font-size: 4rem\">⎛⎝≥⏝⏝≤⎠⎞This is plain text example⎛⎝｡◕⏝⏝◕｡⎠⎞</h1>";
        return new PlainTextResponse(txt);
    }

    public static void main(String[] args) {
        new PlainTextExample().start();
    }
}
