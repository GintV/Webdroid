package com.example.eimantas.gyroacc;

import android.os.Build;
import android.util.Log;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Created by Eimantas on 2016-03-07.
 */
public class WebSocketControl extends WebSocketClient {

    TextView messageCounter;

    public WebSocketControl(URI serverURI) {
        super(serverURI);
    }

    public WebSocketControl(URI serverURI, TextView textView) {
        super(serverURI);
        messageCounter = textView;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i("Websocket", "Opened");
        this.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
    }

    @Override
    public void onMessage(String message) {
        if (messageCounter != null) {
            int cnt = Integer.parseInt(messageCounter.getText().toString());
            cnt++;
            messageCounter.setText(Integer.toString(cnt));
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i("Websocket", "Closed " + reason);
    }

    @Override
    public void onError(Exception ex) {
        Log.i("Websocket", "Error " + ex.getMessage());
    }
}
