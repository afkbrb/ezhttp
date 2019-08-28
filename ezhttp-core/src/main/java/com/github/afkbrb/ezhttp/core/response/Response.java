package com.github.afkbrb.ezhttp.core.response;

import com.github.afkbrb.ezhttp.core.conf.Config;
import com.github.afkbrb.ezhttp.core.protocol.HttpStatus;
import com.github.afkbrb.ezhttp.core.protocol.HttpVersion;
import com.github.afkbrb.ezhttp.core.util.TimeUtil;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Response entity.
 */
public class Response {

    protected StatusLine statusLine;
    protected Map<String, String> headers;
    protected byte[] body;
    private ByteBuffer responseBuffer;

    public Response() {
        this(HttpStatus.OK_200);
    }

    public Response(HttpStatus status) {
        statusLine = new StatusLine(HttpVersion.HTTP1_1, status);
        headers = new HashMap<>();
        body = new byte[0];
        headers.put("Date", TimeUtil.toRFC822(ZonedDateTime.now()));
        headers.put("Server", "com/github/afkbrb/ezhttp/core");
        headers.put("Connection", "Closed"); // TODO keep-alive
    }

    public ByteBuffer getResponseBuffer() {

        if (responseBuffer == null) {
            headers.put("Content-Length", String.valueOf(body.length));
            StringBuilder sb = new StringBuilder();
            String httpVersion = statusLine.getHttpVersion().getName();
            String statusCode = String.valueOf(statusLine.getStatus().getCode());
            String reasonPhrase = statusLine.getStatus().getMessage();

            sb.append(httpVersion).append(" ").append(statusCode).append(" ").append(reasonPhrase).append("\r\n");

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
            }

            sb.append("\r\n");

            byte[] statusLineAndHeaders = new byte[0];
            try {
                statusLineAndHeaders = sb.toString().getBytes(Config.CHARSET);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            responseBuffer = ByteBuffer.allocate(statusLineAndHeaders.length + body.length);
            responseBuffer.put(statusLineAndHeaders);

            responseBuffer.put(body);
            responseBuffer.flip();
        }
        return responseBuffer;
    }
}

class StatusLine {

    private HttpVersion httpVersion;
    private HttpStatus status;

    public StatusLine(HttpVersion httpVersion, HttpStatus status) {
        this.httpVersion = httpVersion;
        this.status = status;
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
