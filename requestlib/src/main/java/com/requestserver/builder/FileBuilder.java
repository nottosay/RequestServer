package com.requestserver.builder;

import com.requestserver.request.FileRequest;

import java.io.File;
import java.util.Map;

import okhttp3.Request;

/**
 * Created by wally.yan on 2016/3/8.
 */
public class FileBuilder extends BaseBuilder {

    private File file;

    public FileBuilder(String url, File file) {
        super(url);
        this.file = file;
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

    @Override
    public Request getRequest() {
        return new FileRequest(url, tag, params, headers, file).buildRequest();
    }
}
