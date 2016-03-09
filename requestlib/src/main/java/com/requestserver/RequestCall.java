package com.requestserver;


import com.requestserver.builder.RequestBuilder;
import com.requestserver.callback.Callback;

import okhttp3.Call;

/**
 * Created by wally.yan on 2015/12/9.
 */
public class RequestCall {
    private RequestBuilder requestBuilder;

    public RequestCall(RequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    public void execute(Callback callback) {
        Call call = RequestClient.getInstance().newCall(requestBuilder.addRequest());
        RequestClient.getInstance().execute(call, callback);
    }
}
