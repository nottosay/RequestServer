package com.windmill;

import com.windmill.callback.Callback;

import rx.Subscriber;

/**
 * Created by wally.yan on 2016/8/2.
 */

public class SubscriberCallback<T> extends Subscriber<T> {

    private Callback<T> callback;

    public SubscriberCallback(Callback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void onCompleted() {
        if (callback != null) {
            callback.onFinish();
        }
    }

    @Override
    public void onError(Throwable e) {
        if (callback != null) {
            callback.onError();
        }
    }

    @Override
    public void onNext(T t) {
        if (callback != null) {
            callback.onSuccess(t);
        }
    }
}
