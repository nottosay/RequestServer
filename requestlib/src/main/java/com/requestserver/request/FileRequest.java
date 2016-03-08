package com.requestserver.request;

import android.text.TextUtils;

import com.requestserver.RequestClient;
import com.requestserver.callback.Callback;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by zhy on 15/12/14.
 */
public class FileRequest extends HttpRequest {
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
            public void onProgress(final long bytesWritten,final long totalSize) {
                RequestClient.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onProgress(bytesWritten * 1.0f / totalSize);
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
