package com.github.afkbrb.ezhttp.core.request;

import com.github.afkbrb.ezhttp.core.protocol.HttpHeader;
import com.github.afkbrb.ezhttp.core.protocol.HttpMethod;
import com.github.afkbrb.ezhttp.core.protocol.HttpVersion;

import java.util.Map;

/**
 * HTTP Request Entity
 */
public class Request {


    private RequestLine requestLine;
    private RequestHeader requestHeader;
    private RequestBody requestBody;

    public Request() {
    }

    public Request(RequestLine requestLine, RequestHeader requestHeader, RequestBody requestBody) {
        this.requestLine = requestLine;
        this.requestHeader = requestHeader;
        this.requestBody = requestBody;
    }


    public String getMethod() {
        return requestLine.getMethod().getName();
    }

    public String getRequestURI() {
        return requestLine.getRequestURI();
    }

    public String getQueryString() {
        return requestLine.getQueryString();
    }

    public String getHttpVersion() {
        return requestLine.getHttpVersion().getName();
    }

    public String getHeader(String header) {
        if (header == null) return null;
        return requestHeader.getHeaderMap().get(header.toLowerCase());
    }

    public String getParam(String key) {
        String ret = requestLine.getParam(key);
        if (ret == null) ret = requestBody.getParam(key);
        return ret;
    }
}

class RequestLine {

    private HttpMethod method;
    private String requestURI;
    private String queryString;
    private Map<String, String> queryMap;
    private HttpVersion httpVersion;

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public Map<String, String> getQueryMap() {
        return queryMap;
    }

    public void setQueryMap(Map<String, String> queryMap) {
        this.queryMap = queryMap;
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
    }

    public String getParam(String key) {
        if (queryMap == null) return null;
        return queryMap.get(key);
    }
}

class RequestHeader {
    private Map<String, String> headerMap;

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public void setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }

    public int getContentLength() {
        return Integer.valueOf(headerMap.getOrDefault(HttpHeader.Content_Length.getName().toLowerCase(), "0"));
    }

    public String getContentType() {
        return headerMap.get(HttpHeader.Content_Type.getName().toLowerCase());
    }
}

class RequestBody {
    private Map<String, String> formMap;
    private Map<String, MimeData> mimeMap;

    public RequestBody() {
    }

    public RequestBody(Map<String, String> formMap, Map<String, MimeData> mimeMap) {
        this.formMap = formMap;
        this.mimeMap = mimeMap;
    }

    public Map<String, String> getFormMap() {
        return formMap;
    }

    public void setFormMap(Map<String, String> formMap) {
        this.formMap = formMap;
    }

    public Map<String, MimeData> getMimeMap() {
        return mimeMap;
    }

    public void setMimeMap(Map<String, MimeData> mimeMap) {
        this.mimeMap = mimeMap;
    }

    public String getParam(String key) {
        if (formMap == null && mimeMap == null) return null;
        if (formMap != null) {
            String ret = formMap.get(key);
            if (ret != null) return ret;
        }
        if (mimeMap != null) {
            MimeData mimeData = mimeMap.get(key);
            if (mimeData == null || (mimeData.getContentType() != null && !mimeData.getContentType().equalsIgnoreCase("text/plain")))
                return null;
            return new String(mimeData.getData());
        }
        return null;
    }
}
