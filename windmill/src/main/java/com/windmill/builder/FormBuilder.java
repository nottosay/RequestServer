package com.windmill.builder;

import com.windmill.request.FormRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Request;

/**
 * Created by wally.yan on 2016/3/8.
 */
public class FormBuilder extends BaseBuilder {

    private List<FormRequest.FileInput> files = new ArrayList<FormRequest.FileInput>();

    public FormBuilder(String url) {
        super(url);
    }

    @Override
    public FormBuilder tag(Object tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public FormBuilder params(Map<String, String> params) {
        this.params = params;
        return this;
    }

    @Override
    public FormBuilder headers(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public FormBuilder addFile(String name, String filename, File file) {
        files.add(new FormRequest.FileInput(name, filename, file));
        return this;
    }

    @Override
    public Request getRequest() {
        return new FormRequest(url, tag, params, headers, files).buildRequest();
    }
}
