package com.requestserver;

import com.requestserver.builder.FileBuilder;
import com.requestserver.builder.FormBuilder;
import com.requestserver.builder.GetBuilder;
import com.requestserver.builder.HeadBuilder;
import com.requestserver.callback.Callback;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wally.yan on 2015/11/8.
 */
public class RequestClient {

    public static final long DEFAULT_MILLISECONDS = 10000;
    private static RequestClient mInstance;
    private OkHttpClient mOkHttpClient;


    private RequestClient(OkHttpClient okHttpClient) {
        if (okHttpClient == null) {
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
            mOkHttpClient = okHttpClientBuilder.build();
        } else {
            mOkHttpClient = okHttpClient;
        }
    }

    public static RequestClient getInstance(OkHttpClient okHttpClient) {
        if (mInstance == null) {
            synchronized (RequestClient.class) {
                if (mInstance == null) {
                    mInstance = new RequestClient(okHttpClient);
                }
            }
        }
        return mInstance;
    }

    public static RequestClient getInstance() {
        if (mInstance == null) {
            synchronized (RequestClient.class) {
                if (mInstance == null) {
                    mInstance = new RequestClient(null);
                }
            }
        }
        return mInstance;
    }

    public static GetBuilder get() {
        return new GetBuilder();
    }

    public static FileBuilder postFile() {
        return new FileBuilder();
    }

    public static FormBuilder post() {
        return new FormBuilder();
    }

    public static HeadBuilder head() {
        return new HeadBuilder();
    }


    public void execute(final Call requestCall, Callback callback) {
        if (callback == null) {
            callback = Callback.CALLBACK_DEFAULT;
        }
        final Callback finalCallback = callback;

        Observable<Object> observable = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                try {
                    Response response = requestCall.execute();
                    if (response.isSuccessful()) {
                        Object object = finalCallback.parseNetworkResponse(response);
                        subscriber.onNext(object);
                    } else {
                        subscriber.onError(new Exception());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
        observable.subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                        finalCallback.onFinish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        finalCallback.onError();
                    }

                    @Override
                    public void onNext(Object o) {
                        finalCallback.onSuccess(o);
                    }
                });
    }


    /**
     * 取消请求
     *
     * @param tag
     */
    public void cancel(Object tag) {
        for (Call call : mOkHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }


    public Call newCall(Request request) {
        return mOkHttpClient.newCall(request);
    }

}
