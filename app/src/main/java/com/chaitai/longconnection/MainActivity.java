package com.chaitai.longconnection;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.chaitai.socket.Callback;
import com.chaitai.socket.ISocketConfigProvider;
import com.chaitai.socket.Request;
import com.chaitai.socket.WebSocketService;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    TextView text;
    EditText input;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = findViewById(R.id.text);
        input = findViewById(R.id.input);
        button = findViewById(R.id.send);

        WebSocketService.init(new ISocketConfigProvider() {
            @Override
            public String getToken() {
                return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOjc4NCwiaWF0IjoxNTczNDM3OTY2LCJzY29wZXMiOlsidXNlciJdLCJpb3JkZXJfdXNlcl9pZCI6bnVsbCwidXNlcm5hbWUiOiJcdTg5ODNcdTYwM2IifQ.UzV4MKX2PYdl6QXgX4pHbs9eOliEGcPC6dTsdw8fRzo";
            }

            @Override
            public String getUrl() {
                return "wss://wss-iorder-dev.cpgroupcloud.com/wss";
            }
        });
        //ws://echo.websocket.org
        //wss://wss-iorder-dev.cpgroupcloud.com/wss
        WebSocketService.getInstance().subscribe(new Request().setChannel("pig/bid:1"), new Callback() {
            @Override
            public void success(String message) {
                Log.i("MainActivity-success", message);
            }

            @Override
            public void fail(String message) {
                Log.i("MainActivity-fail", message);
            }
        });
        Disposable subscribe = Observable.just("").subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {

            }
        });
        Log.e("class", subscribe.getClass().toString());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //客户端请求：{"op":"login","args":{"token":"JWT Token"}}
                Request request = new Request();
                request.setOp("test");
                //request.getArgs().put("token", "1234564513");

                WebSocketService.getInstance().send(request, new Callback() {
                    @Override
                    public void success(String message) {

                    }

                    @Override
                    public void fail(String message) {

                    }
                });
            }
        });
    }

}
