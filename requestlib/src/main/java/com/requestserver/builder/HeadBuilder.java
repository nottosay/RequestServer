package com.requestserver.builder;

import com.requestserver.request.HeadRequest;

import java.util.Map;

import okhttp3.Request;

/**
 * Created by wally.yan on 2016/3/8.
 */
public class HeadBuilder extends RequestBuilder {

    @Override
    public HeadBuilder url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public HeadBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public HeadBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }

    @Override
    public HeadBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    @Override
    public Request addRequest() {
        return new HeadRequest(url, tag, params, headers).buildRequest();
    }
}
