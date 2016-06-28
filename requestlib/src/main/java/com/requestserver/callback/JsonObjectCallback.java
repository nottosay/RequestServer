package com.requestserver.callback;

import com.requestserver.response.NetworkResponse;

/**
 * Created by wally.yan on 2016/6/28.
 */

public abstract class JsonObjectCallback<T> extends Callback<T> {

    @Override
    public T parseNetworkResponse(NetworkResponse networkResponse) throws Exception {
        return null;
    }


}
