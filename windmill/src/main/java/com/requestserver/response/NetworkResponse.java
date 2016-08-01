package com.requestserver.response;

/**
 * Created by wally.yan on 2016/6/28.
 */

public class NetworkResponse {

    //response code
    public int code;

    //response body
    public String body;

    //response
    public okhttp3.Response httpResponse;
}
