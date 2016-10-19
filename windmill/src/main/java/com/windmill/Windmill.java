package com.windmill;

import android.content.Context;

import com.windmill.builder.FileBuilder;
import com.windmill.builder.FormBuilder;
import com.windmill.builder.GetBuilder;
import com.windmill.builder.HeadBuilder;
import com.windmill.builder.PutBuilder;
import com.windmill.cache.Cache;
import com.windmill.cache.DiskBasedCache;

import java.io.File;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by wally.yan on 2015/11/8.
 */
public class Windmill {

    /**
     * Default on-disk cache directory.
     */
    private static final String DEFAULT_CACHE_DIR = "requestServer";

    private static Windmill mInstance;
    private OkHttpClient mOkHttpClient;
    private Cache mCache;
    private static Context mContext;

    private Windmill() {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        mOkHttpClient = okHttpClientBuilder.build();
        File cacheDir = new File(mContext.getCacheDir(), DEFAULT_CACHE_DIR);
        mCache = new DiskBasedCache(cacheDir);
        mCache.initialize();
    }


    static Windmill getInstance() {
        if (mInstance == null) {
            synchronized (Windmill.class) {
                if (mInstance == null) {
                    mInstance = new Windmill();
                }
            }
        }
        return mInstance;
    }

    public static GetBuilder get(Context context, String url) {
        mContext = context;
        return new GetBuilder(url);
    }

    public static FileBuilder upload(Context context, String url, File file) {
        mContext = context;
        return new FileBuilder(url, file);
    }

    public static FileBuilder upload(Context context, String url, String filePath) {
        mContext = context;
        return new FileBuilder(url, new File(filePath));
    }

    public static FormBuilder post(Context context, String url) {
        mContext = context;
        return new FormBuilder(url);
    }

    public static PutBuilder put(Context context, String url) {
        mContext = context;
        return new PutBuilder(url);
    }

    public static HeadBuilder head(Context context, String url) {
        mContext = context;
        return new HeadBuilder(url);
    }

    OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    /**
     * 取消请求
     *
     * @param tag
     */
    public static void cancel(Object tag) {
        OkHttpClient okHttpClient = Windmill.getInstance().getOkHttpClient();
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

    public Cache getCache() {
        return mCache;
    }

    Call newCall(Request request) {
        return mOkHttpClient.newCall(request);
    }

    /**
     * 清空缓存
     */
    public static void clearCache(){
        Windmill windmill = Windmill.getInstance();
        windmill.getCache().clear();
    }
}
