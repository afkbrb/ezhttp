package com.github.afkbrb.ezhttp.core.request;

import com.github.afkbrb.ezhttp.core.conf.Config;
import com.github.afkbrb.ezhttp.core.exception.IllegalRequestException;
import com.github.afkbrb.ezhttp.core.protocol.HttpMethod;
import com.github.afkbrb.ezhttp.core.protocol.HttpVersion;
import com.github.afkbrb.ezhttp.core.util.BytesUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestParser {

    /**
     * Assume that the size of the head is less than 8KB.
     */
    public static final int BUFSIZE = 8192;

    /**
     * Parse the HTTP request line, request header and request body.
     *
     * @param client The client channel.
     * @return HTTP Request entity instance.
     * @throws IOException
     */
    public static Request parseRequest(SocketChannel client) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(BUFSIZE);
        client.read(buffer);
        buffer.flip();
        int remaining = buffer.remaining();
        byte[] bytes = new byte[remaining];
        buffer.get(bytes);
        int crlf = BytesUtil.indexOf(bytes, "\r\n");
        int crlf2 = BytesUtil.indexOf(bytes, "\r\n\r\n");

        if (crlf == -1 || crlf2 == -1) {
            throw new IllegalRequestException("Illegal request format");
        }

        byte[] requestLineSrc = Arrays.copyOfRange(bytes, 0, crlf);
        RequestLine requestLine = parseRequestLine(requestLineSrc);

        byte[] requestHeaderSrc = Arrays.copyOfRange(bytes, crlf + 2, crlf2);
        RequestHeader requestHeader = parseRequestHeader(requestHeaderSrc);

        buffer.position(crlf2 + 4);
        int contentLength = requestHeader.getContentLength();
        ByteBuffer bodyBuffer = ByteBuffer.allocate(contentLength);
        bodyBuffer.put(buffer); // Put the part of the body in buffer to bodyBuffer.
        while (bodyBuffer.hasRemaining()) { // Read the remaining part of the body to bodyBuffer.
            client.read(bodyBuffer);
        }
        byte[] body = bodyBuffer.array();
        RequestBody requestBody = parseRequestBody(body, requestHeader);

        return new Request(requestLine, requestHeader, requestBody);
    }

    /**
     * Construct a RequestLine instance from request line.
     * Request-Line = Method SP Request-URI SP HTTP-Version CRLF
     *
     * @param src
     * @return
     */
    private static RequestLine parseRequestLine(byte[] src) {
        RequestLine requestLine = new RequestLine();
        try {
            String str = new String(src, Config.CHARSET);
            String[] split = str.split(" ");
            String method = split[0];
            String uri = split[1];
            String version = split[2];
            requestLine.setMethod(HttpMethod.parseMethod(method));
            requestLine.setRequestURI(uri);
            requestLine.setHttpVersion(HttpVersion.parseHttpVersion(version));
            int index = uri.indexOf('?');
            if (index != -1) {
                String queryString = uri.substring(index + 1);
                requestLine.setQueryString(decode(queryString));
                // Notice that we cannot decode the queryString first in the parseQueryParams method.
                // Otherwise, if the key or value of some param contains character like '&' or '=',
                // the param will be parsed by mistake.
                Map<String, String> queryMap = parseQueryParams(queryString);
                requestLine.setQueryMap(queryMap);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return requestLine;
    }

    /**
     * Construct a RequestHeader instance from request header.
     *
     * @param src
     * @return
     */
    private static RequestHeader parseRequestHeader(byte[] src) {
        RequestHeader requestHeader = new RequestHeader();
        Map<String, String> headers = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new StringReader(new String(src, Config.CHARSET)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(":");
                if (split.length != 2)
                    continue;
                headers.put(split[0].trim().toLowerCase(), split[1].trim());
            }
            requestHeader.setHeaderMap(headers);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requestHeader;
    }

    /**
     * Construct a RequestBody instance from request body.
     *
     * @param src
     * @param header
     * @return
     */
    private static RequestBody parseRequestBody(byte[] src, RequestHeader header) {
        if (src.length == 0) return new RequestBody();
        Map<String, String> formMap = new HashMap<>();
        Map<String, MimeData> mimeMap = new HashMap<>();

        String contentType = header.getContentType();
        if (contentType.contains("application/x-www-form-urlencoded")) {
            try {
                String body = new String(src, Config.CHARSET);
                formMap = parseQueryParams(body);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (contentType.contains("multipart/form-data")) {
            int boundaryValueIndex = contentType.indexOf("boundary=");
            String boundary = contentType.substring(boundaryValueIndex + "boundary=".length());
            mimeMap = parseMimeMap(src, boundary);
        }

        return new RequestBody(formMap, mimeMap);
    }

    private static Map<String, String> parseQueryParams(String queryString) {
        Map<String, String> queryMap = new HashMap<>();
        String[] kvPairs = queryString.split("&");
        for (String kvPair : kvPairs) {
            String[] kv = kvPair.split("=");
            if (kv.length != 2) continue;
            queryMap.put(decode(kv[0]), decode(kv[1]));
        }
        return queryMap;
    }

    private static Map<String, MimeData> parseMimeMap(byte[] src, String boundary) {
        boundary = "--" + boundary;

        Map<String, MimeData> map = new HashMap<>();
        int startIndex, endIndex;
        List<Integer> allBoundaryIndexes = BytesUtil.findAll(src, boundary);
        // Process empty form.
        if (allBoundaryIndexes.size() == 0)
            return map;

        // Process each segment.
        for (int i = 0; i < allBoundaryIndexes.size() - 1; i++) { // There are allBoundaryIndexes.size() - 1 segments to process.
            startIndex = allBoundaryIndexes.get(i);
            endIndex = allBoundaryIndexes.get(i + 1);
            byte[] segment = Arrays.copyOfRange(src, startIndex + boundary.length() + 2, endIndex);// Skip \r\n.

            int lineEndIndex = BytesUtil.indexOf(segment, "\r\n");
            byte[] firstLine = Arrays.copyOfRange(segment, 0, lineEndIndex);

            String control;
            String contentType = null;
            String filename = null;
            byte[] data = null;

            int dataStartIndex;
            List<Integer> allQuotationIndexes = BytesUtil.findAll(firstLine, "\"");
            control = new String(Arrays.copyOfRange(firstLine, allQuotationIndexes.get(0) + 1, allQuotationIndexes.get(1)));
            // Without filename.
            // Content-Disposition: form-data; name="submit-name"
            if (allQuotationIndexes.size() == 2) {
                dataStartIndex = lineEndIndex + 4;
                data = Arrays.copyOfRange(segment, dataStartIndex, segment.length - 2);
            }
            // With filename.
            // Content-Disposition: form-data; name="files"; filename="file.txt"
            if (allQuotationIndexes.size() == 4) {
                filename = new String(Arrays.copyOfRange(firstLine, allQuotationIndexes.get(2) + 1, allQuotationIndexes.get(3)));
                int headEndIndex = BytesUtil.indexOf(segment, "\r\n\r\n");
                // Find contentType, if not found, default is "text/plain".
                // Content-Disposition: form-data; name="files"; filename="file.txt"
                // Content-Type: text/plain
                contentType = headEndIndex == lineEndIndex ? "text/plain" :
                        new String(Arrays.copyOfRange(segment, lineEndIndex + 16, headEndIndex));
                dataStartIndex = headEndIndex + 4;
                data = Arrays.copyOfRange(segment, dataStartIndex, segment.length);
            }

            map.put(control, new MimeData(contentType, filename, data));
        }
        return map;
    }

    private static String decode(String src) {
        try {
            return URLDecoder.decode(src, Config.CHARSET);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
