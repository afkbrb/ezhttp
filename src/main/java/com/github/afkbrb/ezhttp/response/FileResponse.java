package com.github.afkbrb.ezhttp.response;

import com.github.afkbrb.ezhttp.conf.Config;
import com.github.afkbrb.ezhttp.protocol.HttpStatus;
import com.github.afkbrb.ezhttp.util.MimeUtil;
import com.github.afkbrb.ezhttp.util.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class FileResponse extends Response {

    public FileResponse(File file) {
        if (file == null) throw new NullPointerException("file cannot be null");
        if (!file.isFile() || !file.canRead()) {
            statusLine.setStatus(HttpStatus.NOT_FOUND_404);
        }
        long lastModified = file.lastModified();
        headers.put("Last-Modified", TimeUtil.toRFC822(ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastModified), ZoneId.systemDefault())));
        String path = file.getAbsolutePath();
        String contentType = MimeUtil.getContentType(path);
        if (contentType.startsWith("text")) {
            contentType += "; charset=" + Config.CHARSET;
        }
        headers.put("Content-Type", contentType);
        try {
            body = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            statusLine.setStatus(HttpStatus.NOT_FOUND_404);
        }
    }

    public FileResponse(String path) {
        this(new File(path));
    }
}
