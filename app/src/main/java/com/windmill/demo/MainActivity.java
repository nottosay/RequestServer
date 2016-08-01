package com.windmill.demo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.gson.Gson;
import com.windmill.Windmill;
import com.windmill.callback.Callback;
import com.windmill.response.WindmillResponse;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends Activity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tv);


        Map<String, String> params = new HashMap<String, String>();
        params.put("customerNo", "CH01018997");
        params.put("_m", "357138051294574");
        params.put("_t", "37190b11-2043-41af-9d6c-4d3178d489c4");
        params.put("userName", "paddy.chen");
        params.put("appVersion", "1.0.1");
        params.put("bundleVersion", "1.1.0");
        params.put("platform", "android");
        Windmill.post(MainActivity.this, "http://annieserver.beta.qunar.com//customer/getCustomerByNo.htm").params(params).build(this).execute(new Callback<CustomerResult>() {
            @Override
            public void onError() {
                textView.setText("失败");
            }

            @Override
            public void onSuccess(CustomerResult response) {
                textView.setText(response.data.customerName);
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
