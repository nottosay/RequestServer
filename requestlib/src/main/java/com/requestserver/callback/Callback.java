package com.requestserver.callback;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by wally.yan on 2015/11/8.
 */
public abstract class Callback<T> {
    /**
     * UI Thread
     *
     * @param request
     */
    public void onStart(Request request) {
    }

    /**
     * UI Thread
     *
     * @param
     */
    public void onFinish() {
    }

    /**
     * UI Thread
     *
     * @param progress
     */
    public void onProgress(float progress) {

    }

    /**
     * Thread Pool Thread
     *
     * @param response
     */
    public abstract T parseNetworkResponse(Response response) throws Exception;

    public abstract void onError(Call call, Exception e);

    public abstract void onResponse(T response);


    public static Callback CALLBACK_DEFAULT = new Callback() {

        @Override
        public Object parseNetworkResponse(Response response) throws Exception {
            return null;
        }

        @Override
        public void onError(Call call, Exception e) {

        }

        @Override
        public void onResponse(Object response) {

        }
    };

}