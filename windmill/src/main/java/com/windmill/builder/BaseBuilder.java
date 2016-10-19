package com.windmill.builder;

import com.windmill.RequestAction;

import java.util.Map;

import okhttp3.Request;

/**
 * Created by wally.yan on 2016/3/8.
 */
public abstract class BaseBuilder {

    protected String url;
    protected Object tag;
    protected boolean cacheEnable;
    protected Map<String, String> params;
    protected Map<String, String> headers;


    public BaseBuilder(String url) {
        this.url = url;
    }

    public abstract BaseBuilder tag(Object tag);

    public abstract BaseBuilder params(Map<String, String> params);

    public abstract BaseBuilder headers(Map<String, String> headers);

    public abstract Request getRequest();

    public void cacheEnable(boolean cacheEnable) {
        this.cacheEnable = cacheEnable;
    }

    public RequestAction build() {
        return new RequestAction(this);
    }

    public String getUrl() {
        return url;
    }

    public boolean isCacheEnable() {
        return cacheEnable;
    }
}
