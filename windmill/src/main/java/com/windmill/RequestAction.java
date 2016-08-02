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
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wally.yan on 2015/12/9.
 */
public class RequestAction {
    private BaseBuilder baseBuilder;

    public RequestAction(BaseBuilder baseBuilder) {
        this.baseBuilder = baseBuilder;
    }

    /**
     * 执行请求
     *
     * @param callback 请求回调
     */
    public <T> void execute(final Callback<T> callback) {
        if (!baseBuilder.isCacheEnable()) {
            requestNetwork(false, callback);
            return;
        }

        Cache cache = Windmill.getInstance().getCache();
        CacheEntry cacheEntry = cache.get(baseBuilder.getUrl());
        if (cacheEntry == null) {
            requestNetwork(true, callback);
            return;
        }

        if (cacheEntry.isExpired()) {
            requestNetwork(true, callback);
            return;
        }

        if (cacheEntry.refreshNeeded()) {
            requestCache(cacheEntry, callback);
        }
        requestNetwork(true, null);
    }

    private <T> void requestCache(final CacheEntry cacheEntry, final Callback<T> callback) {
        Observable.create(new Observable.OnSubscribe<T>() {
                              @Override
                              public void call(Subscriber<? super T> subscriber) {
                                  try {
                                      subscriber.onStart();
                                      WindmillResponse windmillResponse = new WindmillResponse();
                                      windmillResponse.code = 200;
                                      windmillResponse.body = new String(cacheEntry.data);
                                      if (callback != null) {
                                          T object = callback.parseResponse(windmillResponse);
                                          subscriber.onNext(object);
                                      }
                                  } catch (Exception e) {
                                      e.printStackTrace();
                                      subscriber.onError(e);
                                  }

                                  subscriber.onCompleted();
                              }
                          }
        ).subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new CustomSubscriber<>(callback));
    }

    private <T> void requestNetwork(final boolean needCache, final Callback<T> callback) {
        final Call call = Windmill.getInstance().newCall(baseBuilder.getRequest());
        Observable.create(new Observable.OnSubscribe<T>() {
                              @Override
                              public void call(Subscriber<? super T> subscriber) {
                                  try {
                                      subscriber.onStart();
                                      WindmillResponse windmillResponse = new WindmillResponse();
                                      Response response = call.execute();
                                      windmillResponse.code = response.code();
                                      windmillResponse.body = response.body().string();
                                      windmillResponse.httpResponse = response;
                                      Cache cache = Windmill.getInstance().getCache();
                                      if (response.isSuccessful()) {
                                          if (needCache) {
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
                                          subscriber.onError(new HttpResponseException(response.code(),response.message()));
                                      }
                                      if (callback != null) {
                                          T object = callback.parseResponse(windmillResponse);
                                          subscriber.onNext(object);
                                      }
                                  } catch (Exception e) {
                                      e.printStackTrace();
                                      subscriber.onError(e);
                                  }

                                  subscriber.onCompleted();
                              }
                          }
        ).subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new CustomSubscriber<>(callback));
    }

}
