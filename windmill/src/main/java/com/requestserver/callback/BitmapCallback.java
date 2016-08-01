package com.requestserver.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.requestserver.response.NetworkResponse;


/**
 * Created by wally.yan on 2015/11/8.
 */
public abstract class BitmapCallback extends Callback<Bitmap> {
    @Override
    public Bitmap parseNetworkResponse(NetworkResponse networkResponse) throws Exception {
        return BitmapFactory.decodeStream(networkResponse.httpResponse.body().byteStream());
    }

}
