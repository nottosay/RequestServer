package com.requestserver.callback;

import com.requestserver.response.NetworkResponse;

import java.io.IOException;


/**
 * Created by wally.yan on 2015/11/8.
 */
public abstract class StringCallback extends Callback<String> {

    @Override
    public String parseNetworkResponse(NetworkResponse networkResponse) throws IOException {
        return networkResponse.body;
    }

}
