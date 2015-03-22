/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.confluenceconfig;

/**
 * Created by yannick on 22/03/15.
 */
public class SetupException extends Exception {
    public SetupException() {
    }

    public SetupException(String message) {
        super(message);
    }

    public SetupException(String message, Throwable cause) {
        super(message, cause);
    }

    public SetupException(Throwable cause) {
        super(cause);
    }
}
