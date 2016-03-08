package com.requestserver.request;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by wally.yan on 2015/5/5.
 */
public class DecorateRequestBody extends RequestBody {

    private RequestBody requestBody;
    private ProgressListner progressListner;

    public DecorateRequestBody(RequestBody requestBody, ProgressListner listner) {
        this.requestBody = requestBody;
        this.progressListner = listner;
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody == null ? -1 : requestBody.contentLength();
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {

        CountingSink countingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(countingSink);

        requestBody.writeTo(bufferedSink);

        bufferedSink.flush();
    }

    protected final class CountingSink extends ForwardingSink {

        private long bytesWritten = 0;

        public CountingSink(Sink sink) {
            super(sink);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWritten += byteCount;
            progressListner.onProgress(bytesWritten, contentLength());
        }

    }

    public interface ProgressListner {
        void onProgress(long bytesWritten, long totalSize);
    }
}
