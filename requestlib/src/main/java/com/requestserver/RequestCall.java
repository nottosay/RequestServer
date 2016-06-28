package com.requestserver;


import com.requestserver.builder.BaseBuilder;
import com.requestserver.cache.Cache;
import com.requestserver.cache.CacheEntry;
import com.requestserver.cache.HttpHeaderParser;
import com.requestserver.callback.Callback;
import com.requestserver.response.NetworkResponse;

import okhttp3.Call;
import okhttp3.Response;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by wally.yan on 2015/12/9.
 */
public class RequestCall {
    private BaseBuilder baseBuilder;

    public RequestCall(BaseBuilder baseBuilder) {
        this.baseBuilder = baseBuilder;
    }

    /**
     * 执行请求
     *
     * @param callback 请求回调
     */
    public void execute(final Callback callback) {
        if (!baseBuilder.isCacheEnable()){
            requestNetwork(false, callback);
            return;
        }

        Cache cache = RequestServer.getInstance().getCache();
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

    private void requestCache(final CacheEntry cacheEntry, final Callback callback) {
        Observable<Object> observable = Observable.create(new Observable.OnSubscribe<Object>() {
              @Override
              public void call(Subscriber<? super Object> subscriber) {
                  try {
                      NetworkResponse networkResponse = new NetworkResponse();
                      networkResponse.code = 200;
                      networkResponse.body = new String(cacheEntry.data);
                      if (callback != null) {
                          Object object = callback.parseNetworkResponse(networkResponse);
                          subscriber.onNext(object);
                      }
                  } catch (Exception e) {
                      e.printStackTrace();
                      subscriber.onError(e);
                  }

                  subscriber.onCompleted();
              }
          }
        );
        observable.subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
                .subscribe(new Observer<Object>() {
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
                    public void onNext(Object o) {
                        if (callback != null) {
                            callback.onSuccess(o);
                        }
                    }
                });
    }

    private void requestNetwork(final boolean needCache, final Callback callback) {
        final Call call = RequestServer.getInstance().newCall(baseBuilder.getRequest());

        Observable<Object> observable = Observable.create(new Observable.OnSubscribe<Object>() {
              @Override
              public void call(Subscriber<? super Object> subscriber) {
                  try {
                      NetworkResponse networkResponse = new NetworkResponse();
                      Response response = call.execute();
                      networkResponse.code = response.code();
                      networkResponse.body = response.body().string();
                      networkResponse.httpResponse = response;
                      Cache cache = RequestServer.getInstance().getCache();
                      if (response.isSuccessful()) {
                          if (needCache){
                              CacheEntry cacheEntry = HttpHeaderParser.parseCacheHeaders(response);
                              cache.put(baseBuilder.getUrl(),cacheEntry);
                          }
                          if (callback != null) {
                              Object object = callback.parseNetworkResponse(networkResponse);
                              subscriber.onNext(object);
                          }
                      } else if(networkResponse.code == 304){
                          CacheEntry cacheEntry = cache.get(baseBuilder.getUrl());
                          if (cacheEntry != null) {
                              networkResponse.code = 304;
                              networkResponse.body = new String(cacheEntry.data);
                              if (callback != null) {
                                  Object object = callback.parseNetworkResponse(networkResponse);
                                  subscriber.onNext(object);
                              }
                          }
                      }else {
                          subscriber.onError(new Exception());
                      }
                  } catch (Exception e) {
                      e.printStackTrace();
                      subscriber.onError(e);
                  }

                  subscriber.onCompleted();
              }
          }
        );
        observable.subscribeOn(Schedulers.io()) // 指定 subscribe() 发生在 IO 线程
        .observeOn(AndroidSchedulers.mainThread()) // 指定 Subscriber 的回调发生在主线程
        .subscribe(new Observer<Object>() {
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
            public void onNext(Object o) {
                if (callback != null) {
                    callback.onSuccess(o);
                }
            }
        });
    }

}
