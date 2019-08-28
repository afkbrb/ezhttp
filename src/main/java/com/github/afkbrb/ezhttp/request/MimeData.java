package com.github.afkbrb.ezhttp.request;

public class MimeData {

    private String contentType;
    private String filename;
    private byte[] data;

    public MimeData() {
    }

    public MimeData(String contentType, String filename, byte[] data) {
        this.contentType = contentType;
        this.filename = filename;
        this.data = data;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
