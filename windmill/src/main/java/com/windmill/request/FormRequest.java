package com.windmill.request;

import com.windmill.callback.Callback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by wally.yan on 2016/3/8.
 */
public class FormRequest extends BaseRequest {

    private List<FileInput> files = new ArrayList<FileInput>();

    public FormRequest(String url, Object tag, Map<String, String> params, Map<String, String> headers, List<FileInput> files) {
        super(url, tag, params, headers);
        this.tag = tag;
        this.params = params;
        this.headers = headers;
        this.files = files;
    }


    @Override
    public RequestBody buildRequestBody() {
        if (files == null || files.isEmpty()) {
            FormBody.Builder builder = new FormBody.Builder();
            addParams(builder);
            return builder.build();
        } else {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            addParams(builder);
            for (FileInput fileInput : files) {
                RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), fileInput.file);
                builder.addFormDataPart(fileInput.key, fileInput.filename, fileBody);
            }
            return builder.build();
        }
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

    public static class FileInput {
        public String key;
        public String filename;
        public File file;

        public FileInput(String name, String filename, File file) {
            this.key = name;
            this.filename = filename;
            this.file = file;
        }
    }
}
