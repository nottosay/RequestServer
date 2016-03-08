package com.requestserver.builder;

import com.requestserver.request.FileRequest;

import java.io.File;
import java.util.Map;

import okhttp3.Request;

/**
 * Created by wally.yan on 2016/3/8.
 */
public class FileBuilder extends RequestBuilder {

    private File file;

    @Override
    public FileBuilder url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public FileBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public FileBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }

    @Override
    public FileBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public FileBuilder file(File file) {
        this.file = file;
        return this;
    }

    @Override
    public Request addRequest() {
        return new FileRequest(url, tag, params, headers, file).buildRequest();
    }
}
