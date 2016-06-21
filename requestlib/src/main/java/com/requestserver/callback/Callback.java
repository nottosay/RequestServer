package com.requestserver.callback;


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
     * @param bytesWritten 以写入大小
     * @param totalSize    总大小
     */
    public void onProgress(long bytesWritten, long totalSize) {

    }

    /**
     * Thread Pool Thread
     *
     * @param response
     */
    public abstract T parseNetworkResponse(Response response) throws Exception;

    public abstract void onError();

    public abstract void onSuccess(T response);

    public static Callback CALLBACK_DEFAULT = new Callback() {

        @Override
        public Object parseNetworkResponse(Response response) throws Exception {
            return null;
        }

        @Override
        public void onSuccess(Object response) {

        }

        @Override
        public void onError() {

        }
    };

}