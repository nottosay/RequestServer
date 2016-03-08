package com.requestserver.callback;

import java.io.IOException;

import okhttp3.Response;

/**
 * Created by wally.yan on 2015/11/8.
 */
public abstract class StringCallback extends Callback<String> {
    @Override
    public String parseNetworkResponse(Response response) throws IOException {
        return response.body().string();
    }

}
