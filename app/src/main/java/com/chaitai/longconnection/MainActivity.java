package com.chaitai.longconnection;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.chaitai.socket.Callback;
import com.chaitai.socket.ISocketConfigProvider;
import com.chaitai.socket.Request;
import com.chaitai.socket.WebSocketService;

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
                return "4567816561";
            }

            @Override
            public String getUrl() {
                return "wss://wss-iorder-dev.cpgroupcloud.com/wss";
            }
        });
        //ws://echo.websocket.org
        //wss://wss-iorder-dev.cpgroupcloud.com/wss
        WebSocketService.getInstance().subscribe(new Request().setChannel("pig/bid:hjhiuoh"), new Callback() {
            @Override
            public void success(String message) {

            }

            @Override
            public void fail(String message) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //客户端请求：{"op":"login","args":{"token":"JWT Token"}}
                Request request = new Request();
                request.setOp("login");
                request.getArgs().put("token", "1234564513");

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
