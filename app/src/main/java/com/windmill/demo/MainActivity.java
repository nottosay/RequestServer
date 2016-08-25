package com.windmill.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;
import com.windmill.Windmill;
import com.windmill.callback.Callback;
import com.windmill.response.WindmillResponse;


public class MainActivity extends Activity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tv);

        Windmill.get(MainActivity.this, "http://gank.io/api/data/Android/2/1").build(this).execute(new Callback<CustomerResult>() {
            @Override
            public void onError() {
                textView.setText("失败");
            }

            @Override
            public void onSuccess(CustomerResult response) {
                textView.setText(response.results.get(0).desc);
            }

            @Override
            public CustomerResult parseResponse(WindmillResponse windmillResponse) throws Exception {
                String body = windmillResponse.body;
                CustomerResult result = new Gson().fromJson(body, CustomerResult.class);
                return result;
            }
        });
    }
}
