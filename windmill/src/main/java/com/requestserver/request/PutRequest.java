package com.requestserver.request;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by wally.yan on 2016/3/8.
 */
public class PutRequest extends BaseRequest {


    public PutRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers) {
        super(url, tag, params, headers);
        this.tag = tag;
        this.params = params;
        this.headers = headers;
    }


    @Override
    public RequestBody buildRequestBody() {
        FormBody.Builder builder = new FormBody.Builder();
        addParams(builder);
        return builder.build();

    }

    @Override
    public Request buildRequest() {
        return builder.put(buildRequestBody()).build();
    }
}
