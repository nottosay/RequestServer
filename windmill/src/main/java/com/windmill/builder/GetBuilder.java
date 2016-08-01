package com.windmill.builder;

import com.windmill.request.GetRequest;

import java.util.Map;

import okhttp3.Request;

/**
 * Created by wally.yan on 2016/3/8.
 */
public class GetBuilder extends BaseBuilder {

    public GetBuilder(String url) {
        super(url);
    }

    @Override
    public GetBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public GetBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }

    @Override
    public GetBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    @Override
    public Request getRequest() {
        return new GetRequest(url, tag, params, headers).buildRequest();
    }
}
