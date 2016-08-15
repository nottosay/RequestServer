package com.windmill;


import com.windmill.builder.BaseBuilder;
import com.windmill.cache.Cache;
import com.windmill.cache.CacheEntry;
import com.windmill.cache.HttpHeaderParser;
import com.windmill.callback.Callback;
import com.windmill.response.WindmillResponse;

import org.apache.http.client.HttpResponseException;

import okhttp3.Call;
import okhttp3.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by wally.yan on 2015/12/9.
 */
public class RequestAction {
    private BaseBuilder baseBuilder;
    private Cache mCache;

    public RequestAction(BaseBuilder baseBuilder) {
        this.baseBuilder = baseBuilder;
        mCache = Windmill.getInstance().getCache();
    }

    /**
     * 执行请求
     *
     * @param callback 请求回调
     */
    public <T> void execute(final Callback<T> callback) {
        Observable<T> observable;
        if (baseBuilder.isCacheEnable()) {
            //先请求cache，cache返回null，再请求网络
            observable = Observable.concat(requestCacheObservable(callback), requestNetworkObservable(callback));
            observable.first(new Func1<T, Boolean>() {
                @Override
                public Boolean call(T t) {
                    return t != null;
                }
            });
        } else {
            //请求网络
            observable = requestNetworkObservable(callback);
        }
        observable.subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new SubscriberCallback<>(callback));

    }

    /**
     * 获取请求cache的Observable
     *
     * @param callback
     * @param <T>
     * @return
     */
    private <T> Observable<T> requestCacheObservable(final Callback<T> callback) {
        return Observable.defer(new Func0<Observable<T>>() {
            @Override
            public Observable<T> call() {
                CacheEntry cacheEntry = mCache.get(baseBuilder.getUrl());
                if (cacheEntry != null) {
                    if (cacheEntry.isExpired()) {
                        return Observable.just(null);
                    }

                    if (cacheEntry.refreshNeeded()) {
                        return Observable.just(null);
                    }
                    WindmillResponse windmillResponse = new WindmillResponse();
                    windmillResponse.code = 200;
                    windmillResponse.body = new String(cacheEntry.data);
                    try {
                        if (callback != null) {
                            T object = callback.parseResponse(windmillResponse);
                            return Observable.just(object);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Observable.error(e);
                    }
                }
                return Observable.just(null);
            }
        });
    }

    /**
     * 获取请求网络结果的Observable
     *
     * @param callback
     * @param <T>
     * @return
     */
    private <T> Observable<T> requestNetworkObservable(final Callback<T> callback) {
        final Call call = Windmill.getInstance().newCall(baseBuilder.getRequest());
        return Observable.defer(new Func0<Observable<T>>() {
            @Override
            public Observable<T> call() {
                T object = null;
                try {
                    WindmillResponse windmillResponse = new WindmillResponse();
                    Response response = call.execute();
                    windmillResponse.code = response.code();
                    windmillResponse.body = response.body().string();
                    windmillResponse.httpResponse = response;
                    Cache cache = Windmill.getInstance().getCache();
                    if (response.isSuccessful()) {
                        if (baseBuilder.isCacheEnable()) {
                            CacheEntry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                            if (cacheEntry != null) {
                                cache.put(baseBuilder.getUrl(), cacheEntry);
                            }
                        }
                    } else if (windmillResponse.code == 304) {//Not Modified
                        CacheEntry cacheEntry = cache.get(baseBuilder.getUrl());
                        if (cacheEntry != null) {
                            windmillResponse.code = 304;
                            windmillResponse.body = new String(cacheEntry.data);
                        }
                    } else {
                        return Observable.error(new HttpResponseException(response.code(), response.message()));
                    }
                    if (callback != null) {
                        object = callback.parseResponse(windmillResponse);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return Observable.error(e);
                }
                return Observable.just(object);
            }
        });
    }

}
