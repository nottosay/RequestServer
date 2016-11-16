package com.windmill.callback;

import com.windmill.response.WindmillResponse;

import java.io.IOException;


/**
 * Created by wally.yan on 2015/11/8.
 */
public abstract class StringCallback extends Callback<String> {

    @Override
    public String parseResponse(WindmillResponse windmillResponse) throws IOException {
        return windmillResponse.httpResponse.body().toString();
    }

}
