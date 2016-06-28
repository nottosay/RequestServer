package com.requestserver;

import android.content.Context;

import com.requestserver.builder.FileBuilder;
import com.requestserver.builder.FormBuilder;
import com.requestserver.builder.GetBuilder;
import com.requestserver.builder.HeadBuilder;
import com.requestserver.builder.PutBuilder;
import com.requestserver.cache.Cache;
import com.requestserver.cache.DiskBasedCache;

import java.io.File;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by wally.yan on 2015/11/8.
 */
public class RequestServer {

    /**
     * Default on-disk cache directory.
     */
    private static final String DEFAULT_CACHE_DIR = "requestServer";

    private static RequestServer mInstance;
    private OkHttpClient mOkHttpClient;
    private Cache mCache;

    private RequestServer() {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        mOkHttpClient = okHttpClientBuilder.build();
    }


    public static RequestServer getInstance() {
        if (mInstance == null) {
            synchronized (RequestServer.class) {
                if (mInstance == null) {
                    mInstance = new RequestServer();
                }
            }
        }
        return mInstance;
    }

    public static GetBuilder get(String url) {
        return new GetBuilder(url);
    }

    public static FileBuilder upload(String url, File file) {
        return new FileBuilder(url, file);
    }

    public static FileBuilder upload(String url, String filePath) {
        return new FileBuilder(url, new File(filePath));
    }

    public static FormBuilder post(String url) {
        return new FormBuilder(url);
    }

    public static PutBuilder put(String url) {
        return new PutBuilder(url);
    }

    public static HeadBuilder head(String url) {
        return new HeadBuilder(url);
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    /**
     * 取消请求
     *
     * @param tag
     */
    public static void cancel(Object tag) {
        OkHttpClient okHttpClient = RequestServer.getInstance().getOkHttpClient();
        for (Call call : okHttpClient.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : okHttpClient.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }

    public void setContext(Context context) {
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
        mCache = new DiskBasedCache(cacheDir);
    }

    public Cache getCache() {
        return mCache;
    }

    public Call newCall(Request request) {
        return mOkHttpClient.newCall(request);
    }

}
