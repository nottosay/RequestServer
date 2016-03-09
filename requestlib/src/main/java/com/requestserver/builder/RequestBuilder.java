package com.requestserver.builder;

import com.requestserver.RequestCall;

import java.util.Map;

import okhttp3.Request;

/**
 * Created by wally.yan on 2016/3/8.
 */
public abstract class RequestBuilder {

    protected String url;
    protected Object tag;
    protected Map<String, String> params;
    protected Map<String, String> headers;


    public abstract RequestBuilder url(String url);

    public abstract RequestBuilder tag(Object tag);

    public abstract RequestBuilder params(Map<String, String> params);

    public abstract RequestBuilder headers(Map<String, String> headers);

    public abstract Request addRequest();

    public RequestCall build() {
        return new RequestCall(this);
    }

}
