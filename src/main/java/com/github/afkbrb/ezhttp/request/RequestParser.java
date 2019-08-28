package com.github.afkbrb.ezhttp.request;

import com.github.afkbrb.ezhttp.conf.Config;
import com.github.afkbrb.ezhttp.exception.IllegalRequestException;
import com.github.afkbrb.ezhttp.protocol.HttpMethod;
import com.github.afkbrb.ezhttp.protocol.HttpVersion;
import com.github.afkbrb.ezhttp.util.BytesUtil;

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


/**
 * 解析请求信息，返回保存请求信息的Request对象
 */
public class RequestParser {

    // Apache默认请求头限制就是8k
    public static final int BUFSIZE = 8192;

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
        bodyBuffer.put(buffer); // 将buffer中的body部分存入bodyBuffer中
        while (bodyBuffer.hasRemaining()) { // 读取流中剩下的部分
            client.read(bodyBuffer);
        }
        byte[] body = bodyBuffer.array();
        RequestBody requestBody = parseRequestBody(body, requestHeader);

        return new Request(requestLine, requestHeader, requestBody);
    }

    // 根据请求行构造RequestLine对象
    // Request-Line = Method SP Request-URI SP HTTP-Version CRLF
    private static RequestLine parseRequestLine(byte[] src) {
        // System.out.println("line:");
        // System.out.println(new String(src));
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
                // 不能先将queryString解码再传入parseQueryParams，否则如queryString键或值本来就含有&和=的话会解析错误
                Map<String, String> queryMap = parseQueryParams(queryString);
                requestLine.setQueryMap(queryMap);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return requestLine;
    }

    // 根据请求头构造RequestHeader对象
    private static RequestHeader parseRequestHeader(byte[] src) {
        // System.out.println("header:");
        // System.out.println(new String(src));
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

    // 构造请求体
    private static RequestBody parseRequestBody(byte[] src, RequestHeader header) {
        // System.out.println("body:");
        // System.out.println(new String(src));
        if (src.length == 0) return new RequestBody();
        Map<String, String> formMap = new HashMap<>();
        Map<String, MimeData> mimeMap = new HashMap<>();

        String contentType = header.getContentType();
        if (contentType.contains("application/x-www-form-urlencoded")) {
            try {
                String body = new String(src, Config.CHARSET);
                // 与queryString类似
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

    // 根据查询字符串创建查询map
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
        //process empty form
        if (allBoundaryIndexes.size() == 0)
            return map;

        //process each segment
        for (int i = 0; i < allBoundaryIndexes.size() - 1; i++) { //there are allBoundaryIndexes.size() - 1 segments to process
            startIndex = allBoundaryIndexes.get(i);
            endIndex = allBoundaryIndexes.get(i + 1);
            byte[] segment = Arrays.copyOfRange(src, startIndex + boundary.length() + 2, endIndex);//去掉\r\n

            int lineEndIndex = BytesUtil.indexOf(segment, "\r\n");
            byte[] firstLine = Arrays.copyOfRange(segment, 0, lineEndIndex);

            String control;
            String contentType = null;
            String filename = null;
            byte[] data = null;

            int dataStartIndex;
            List<Integer> allQuotationIndexes = BytesUtil.findAll(firstLine, "\"");
            control = new String(Arrays.copyOfRange(firstLine, allQuotationIndexes.get(0) + 1, allQuotationIndexes.get(1)));
            // without filename
            // Content-Disposition: form-data; name="submit-name"
            if (allQuotationIndexes.size() == 2) {
                dataStartIndex = lineEndIndex + 4;
                data = Arrays.copyOfRange(segment, dataStartIndex, segment.length - 2);
            }
            // with filename
            // Content-Disposition: form-data; name="files"; filename="file1.txt"
            if (allQuotationIndexes.size() == 4) {
                filename = new String(Arrays.copyOfRange(firstLine, allQuotationIndexes.get(2) + 1, allQuotationIndexes.get(3)));
                int headEndIndex = BytesUtil.indexOf(segment, "\r\n\r\n");
                // find contentType, if not found, default is "text/plain"
                // Content-Disposition: form-data; name="files"; filename="file1.txt"
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
