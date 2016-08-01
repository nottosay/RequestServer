package com.windmill.callback;


import com.windmill.response.WindmillResponse;

/**
 * Created by wally.yan on 2015/11/8.
 */
public abstract class Callback<T> {

    /**
     * UI Thread
     */
    public void onStart() {
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
     * @param windmillResponse
     */
    public abstract T parseResponse(WindmillResponse windmillResponse) throws Exception;

    public abstract void onError();

    public abstract void onSuccess(T response);

}