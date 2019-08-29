package com.github.afkbrb.ezhttp.example;

import com.github.afkbrb.ezhttp.core.Server;
import com.github.afkbrb.ezhttp.core.request.Request;
import com.github.afkbrb.ezhttp.core.response.FileResponse;
import com.github.afkbrb.ezhttp.core.response.NotFoundResponse;
import com.github.afkbrb.ezhttp.core.response.Response;

import java.io.File;
import java.io.IOException;

public class FileServer extends Server {

    private final String root;

    public FileServer(String root, String host, int port) throws IOException {
        super(host, port);
        this.root = processRoot(root);
    }

    public FileServer(String root, int port) throws IOException {
        this(root, "localhost", port);
    }

    public FileServer(String root) throws IOException {
        this(root, 2333);
    }

    @Override
    protected Response handle(Request request) {

        String uri = request.getRequestURI();
        // Delete queryString part.
        int index;
        if ((index = uri.indexOf('?')) != -1) {
            uri = uri.substring(0, index);
        }

        File file = new File(root, uri);
        try {
            // Make sure the file is in the root directory.
            if (file.isDirectory() || !file.canRead() || !file.getCanonicalPath().startsWith(root)) {
                return new NotFoundResponse();
            }
        } catch (IOException e) {

        }
        return new FileResponse(file);
    }

    private String processRoot(String root) throws IOException {
        if (root == null || root.equals("")) throw new IllegalArgumentException("Root cannot be null");
        // Delete '/'
        if (root.endsWith("/") || root.endsWith("\\")) {
            root = root.substring(0, root.length() - 1);
        }
        File dir = new File(root);
        if (!dir.isDirectory()) {
            throw new IOException(root + " is not a directory");
        }
        return root;
    }

    public static void main(String[] args) throws IOException {
        Server server = new FileServer("path\\to\\root\\dir");
        server.start();
    }
}
