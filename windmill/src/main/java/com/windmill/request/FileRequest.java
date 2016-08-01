package com.windmill.request;

import android.text.TextUtils;

import com.windmill.callback.Callback;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zhy on 15/12/14.
 */
public class FileRequest extends BaseRequest {
    private static MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream");

    private File file;
    private MediaType mediaType;

    public FileRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers, File file) {
        super(url, tag, params, headers);
        this.tag = tag;
        this.params = params;
        this.headers = headers;
        this.file = file;
    }

    public FileRequest setMediaType(String mediaType) {
        if (TextUtils.isEmpty(mediaType)) {
            this.mediaType = MEDIA_TYPE_STREAM;
        } else {
            this.mediaType = MediaType.parse(mediaType);
        }
        return this;
    }

    public FileRequest setMediaType(MediaType mediaType) {
        if (mediaType == null) {
            this.mediaType = MEDIA_TYPE_STREAM;
        } else {
            this.mediaType = mediaType;
        }
        return this;
    }


    @Override
    public RequestBody buildRequestBody() {
        return RequestBody.create(mediaType, file);
    }

    @Override
    public RequestBody wrapRequestBody(RequestBody requestBody, final Callback callback) {
        if (callback == null) {
            return requestBody;
        }
        DecorateRequestBody decorateRequestBody = new DecorateRequestBody(requestBody, new DecorateRequestBody.ProgressListner() {
            @Override
            public void onProgress(final long bytesWritten, final long totalSize) {
                long[] longs = new long[]{bytesWritten, totalSize};
                Observable.just(longs).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<long[]>() {
                    @Override
                    public void call(long[] longs) {
                        callback.onProgress(longs[0], longs[1]);
                    }
                });
            }
        });
        return decorateRequestBody;
    }

    @Override
    public Request buildRequest() {
        return builder.post(buildRequestBody()).build();
    }
}
