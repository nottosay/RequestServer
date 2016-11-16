package com.windmill.demo;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.windmill.Windmill;
import com.windmill.callback.BitmapCallback;
import com.windmill.callback.FileCallBack;

import java.io.File;


public class MainActivity extends Activity {
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.img);

        Windmill.get(MainActivity.this, "http://qhh.qunarzz.com/qhh/crm/201611/10/1fece0cb254f1f45891bc9ee9fed72a9/1478759437583-03139cdb-0622-4504-92d1-5131b1ccdfef.JPEG_r_1920x1080_b3d3fa93.jpeg").build().execute(new BitmapCallback() {
            @Override
            public void onError() {
            }

            @Override
            public void onSuccess(Bitmap response) {
                imageView.setImageBitmap(response);
            }
        });

        //检查权限
        /*if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //进入到这里代表没有权限.
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        } else {
            doRequest();
        }*/
    }

    private void doRequest(){
        Windmill.get(MainActivity.this, "http://qhh.qunarzz.com/qhh/crm/201611/10/1fece0cb254f1f45891bc9ee9fed72a9/1478759437583-03139cdb-0622-4504-92d1-5131b1ccdfef.JPEG_r_1920x1080_b3d3fa93.jpeg").build().execute(new FileCallBack(Environment.getExternalStorageDirectory()+"","test111.jpg") {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(File response) {
                Log.i("wally","onSuccess");
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
