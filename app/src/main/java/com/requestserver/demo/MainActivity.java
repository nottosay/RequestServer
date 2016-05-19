package com.requestserver.demo;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.requestserver.RequestClient;
import com.requestserver.callback.Callback;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;



public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Map<String, String> params = new HashMap<String, String>();
        params.put("accountName", "phone");
        params.put("accountPwd", "12345");
        RequestClient.post().params(params).url("http://220.162.244.140:4321/Account/Login").build().execute(new Callback() {
            @Override
            public Object parseNetworkResponse(Response response) throws Exception {
                String result = response.body().string();
                Log.i("wally",result);
                return null;
            }

            @Override
            public void onError() {
                Log.i("wally","onError");
            }

            @Override
            public void onSuccess(Object response) {

            }

            @Override
            public void onFailure(String response) {

            }
        });
    }
}
