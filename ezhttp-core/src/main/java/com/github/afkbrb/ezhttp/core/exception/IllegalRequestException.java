package com.github.afkbrb.ezhttp.core.exception;

import java.io.IOException;

public class IllegalRequestException extends IOException {

    public IllegalRequestException() {
        super();
    }

    public IllegalRequestException(String message) {
        super(message);
    }
}
