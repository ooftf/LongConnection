package com.chaitai.longconnection;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaitai.socket.Callback;
import com.chaitai.socket.HiClient;

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
        //ws://echo.websocket.org
        //wss://wss-iorder-dev.cpgroupcloud.com/wss
        final HiClient client = new HiClient("wss://wss-iorder-dev.cpgroupcloud.com/wss");
        client.subscribe("123", new Callback() {
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
                client.send(input.getText().toString(), new Callback() {
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
