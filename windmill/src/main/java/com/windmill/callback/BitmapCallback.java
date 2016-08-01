package com.windmill.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.windmill.response.WindmillResponse;


/**
 * Created by wally.yan on 2015/11/8.
 */
public abstract class BitmapCallback extends Callback<Bitmap> {
    @Override
    public Bitmap parseResponse(WindmillResponse windmillResponse) throws Exception {
        return BitmapFactory.decodeStream(windmillResponse.httpResponse.body().byteStream());
    }

}
