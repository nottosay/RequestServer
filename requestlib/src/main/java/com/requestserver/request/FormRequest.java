package com.requestserver.request;

import com.requestserver.RequestClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by wally.yan on 2016/3/8.
 */
public class FormRequest extends HttpRequest {

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
            RequestBody requestBody = builder.build();
            DecorateRequestBody decorateRequestBody = new DecorateRequestBody(requestBody, new DecorateRequestBody.ProgressListner() {
                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    RequestClient.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });
                }
            });
            return decorateRequestBody;
        }
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
