package com.example.eimantas.gyroacc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;

public class ChatActivity extends AppCompatActivity {

    private WebSocketControl webSocket;
    private EditText messageBox;
    private TextView chatBox;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageBox = (EditText) findViewById(R.id.editTextMessage);
        chatBox = (TextView) findViewById(R.id.textViewChatBox);
        scrollView = (ScrollView) findViewById(R.id.scrollView);

        messageBox.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    sendMessage(null);
                    return true;
                }
                return false;
            }
        });

        try {
            webSocket = new WebSocketControl(new URI("ws://218.gaikaz.tk:3000")) {
                @Override
                public void onMessage(String message) {
                    messageReceived(message);
                }
            };
            webSocket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    protected void onStop() {
        super.onStop();
        webSocket.close();
    }

    public void sendMessage(View view) {
        try {
            chatBox.append("Client: " + messageBox.getText().toString() + System.getProperty("line.separator"));
            webSocket.send(messageBox.getText().toString());
        }
        catch (Exception ex) {
            chatBox.append("Error: " + ex.toString());
        }
        messageBox.setText("");
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

    protected void messageReceived(String message) {
        final String msg = message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatBox.append("Server: " + msg + System.getProperty("line.separator"));
            }
        });
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}
