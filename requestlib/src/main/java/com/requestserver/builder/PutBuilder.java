package com.requestserver.builder;

import com.requestserver.request.PutRequest;

import java.util.Map;

import okhttp3.Request;

/**
 * Created by wally.yan on 2016/3/8.
 */
public class PutBuilder extends BaseBuilder {

    public PutBuilder(String url) {
        super(url);
    }

    @Override
    public PutBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public PutBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }

    @Override
    public PutBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    @Override
    public Request getRequest() {
        return new PutRequest(url, tag, params, headers).buildRequest();
    }
}
