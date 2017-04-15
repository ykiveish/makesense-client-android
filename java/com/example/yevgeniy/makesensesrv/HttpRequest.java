package com.example.yevgeniy.makesensesrv;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by yevgeniy on 06/02/17.
 */

public class HttpRequest {
    String type = null;
    String server = null;
    String payload = null;
    Boolean isQueryString = false;
    private Boolean _isStreamPublish = false;
    public byte[] StreamBuffer;
    List<ServiceCallback> callbacks;

    HttpRequest (String type, String server, String payload, Boolean isQueryString, List<ServiceCallback> callbacks) {
        this.type = type;
        this.server = server;
        this.payload = payload;
        this.isQueryString = isQueryString;
        this.callbacks = callbacks;
    }

    public void SetStreamPublish (boolean flag) {
        this._isStreamPublish = flag;
    }

    public boolean GetStreamPublish () {
        return this._isStreamPublish;
    }
}
