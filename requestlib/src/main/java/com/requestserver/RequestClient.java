package com.requestserver;

import android.os.Handler;
import android.os.Looper;

import com.requestserver.builder.FileBuilder;
import com.requestserver.builder.FormBuilder;
import com.requestserver.builder.GetBuilder;
import com.requestserver.builder.HeadBuilder;
import com.requestserver.callback.Callback;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by wally.yan on 2015/11/8.
 */
public class RequestClient {

    public static final long DEFAULT_MILLISECONDS = 10000;
    private static RequestClient mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mHandler;


    private RequestClient(OkHttpClient okHttpClient) {
        if (okHttpClient == null) {
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
            mHandler = new Handler(Looper.getMainLooper());
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

        requestCall.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                sendFailResultCallback(call, e, finalCallback);
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                if (response.code() >= 400 && response.code() <= 599) {
                    try {
                        sendFailResultCallback(call, new RuntimeException(response.body().string()), finalCallback);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                try {
                    Object object = finalCallback.parseNetworkResponse(response);
                    sendSuccessResultCallback(object, finalCallback);
                } catch (Exception e) {
                    sendFailResultCallback(call, e, finalCallback);
                }

            }
        });
    }


    private void sendFailResultCallback(final Call call, final Exception e, final Callback callback) {
        if (callback == null) return;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(call, e);
                callback.onFinish();
            }
        });
    }

    private void sendSuccessResultCallback(final Object object, final Callback callback) {
        if (callback == null) return;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onResponse(object);
                callback.onFinish();
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

    /**
     * 在UI线程运行
     *
     * @param runnable
     */
    public void runOnUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }


    public Call newCall(Request request) {
        return mOkHttpClient.newCall(request);
    }

}
