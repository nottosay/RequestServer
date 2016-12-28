package com.windmill.demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.windmill.Windmill;
import com.windmill.callback.BitmapCallback;
import com.windmill.callback.Callback;
import com.windmill.callback.FileCallBack;
import com.windmill.response.WindmillResponse;

import java.io.File;


public class MainActivity extends Activity {
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.img);

        Windmill.get(MainActivity.this, "https://qhh.qunarzz.com/qhh/crm/201611/10/1fece0cb254f1f45891bc9ee9fed72a9/1478759437583-03139cdb-0622-4504-92d1-5131b1ccdfef.JPEG_r_1920x1080_b3d3fa93.jpeg").build().execute(new BitmapCallback() {
            @Override
            public void onError() {
            }

            @Override
            public void onSuccess(Bitmap response) {
                imageView.setImageBitmap(response);
            }
        });

        //检查权限
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //进入到这里代表没有权限.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        } else {
            doRequest();
        }
    }

    private void doRequest(){
        Windmill.post(MainActivity.this, "http://crmimg.beta.qunar.com/imageManager/queryImageByApp.do").build().execute(new Callback<ImageInfoResult>() {
            @Override
            public void onSuccess(ImageInfoResult imageInfoResult) {
                if (imageInfoResult != null && imageInfoResult.data != null && !TextUtils.isEmpty(imageInfoResult.data.imageUrl)) {
                    Windmill.get(MainActivity.this, "https://l-swift1.ops.dev.cn0.qunar.com/test/crm/201611/15/1fece0cb254f1f45891bc9ee9fed72a9/1479199718738-a25323aa-595e-4452-ba07-0f86a065d931.JPEG_r_1920x1080_1c410ebd.jpeg").build().execute(new FileCallBack(Environment.getExternalStorageDirectory()+"","test111.jpg") {
                        @Override
                        public void onError() {

                        }

                        @Override
                        public void onSuccess(File response) {
                            Log.i("wally","onSuccess");
                        }
                    });
                }
            }

            @Override
            public void onError() {

            }

            @Override
            public ImageInfoResult parseResponse(WindmillResponse windmillResponse) throws Exception {
                ImageInfoResult result = null;
                try {
                    Gson gson = new Gson();
                    String data = windmillResponse.httpResponse.body().string();
                    result = gson.fromJson(data, ImageInfoResult.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 101:
                if(grantResults.length >0 &&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //用户同意授权
                    doRequest();
                }else{
                    //用户拒绝授权
                }
                break;
        }
    }
}
