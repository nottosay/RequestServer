package com.windmill.request;

import java.util.Map;

import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by wally.yan on 2016/3/8.
 */
public class GetRequest extends BaseRequest {

    public GetRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers) {
        super(url, tag, params, headers);
        this.tag = tag;
        this.params = params;
        this.headers = headers;
    }

    @Override
    public RequestBody buildRequestBody() {
        url = appendParams(url, params);
        return null;
    }

    @Override
    public Request buildRequest() {
        return builder.get().build();
    }

    private String appendParams(String url, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append(url + "?");
        if (params != null && !params.isEmpty()) {
            for (String key : params.keySet()) {
                sb.append(key).append("=").append(params.get(key)).append("&");
            }
        }

        sb = sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
