package com.example.eimantas.gyroacc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;

public class ChatActivity extends Activity {

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

        chatBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollDown();
            }
        });

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
            if (!openWebSocket(new URI("ws://218.gaikaz.tk:80"))) {
                openWebSocket(new URI("ws://webdroid.cf:80"));
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    protected void onDestroy() {
        super.onDestroy();
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
        scrollDown();
    }

    protected void messageReceived(String message) {
        final String msg = message;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatBox.append("Server: " + msg + System.getProperty("line.separator"));
            }
        });
        scrollDown();
    }

    protected void scrollDown() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    protected boolean openWebSocket(URI uri) {
        webSocket = new WebSocketControl(uri) {
           @Override
           public void onMessage(String message) {
                    messageReceived(message);
                }
        };
        webSocket.connect();
        return webSocket.isConnected();
    }

    public void changeWs(View view) {
        AlertDialog.Builder diaBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View inflatedView = inflater.inflate(R.layout.dialog_set_ws, null);
        diaBuilder.setView(inflatedView);
        final EditText address = (EditText) inflatedView.findViewById(R.id.address);
        address.setTextColor(Color.BLACK);
        address.setText(webSocket.getURI().toString());
        address.setSelection(address.getText().toString().length());

        diaBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String uriStr = address.getText().toString();
                if (webSocket.getURI().toString().compareTo(uriStr) != 0) {
                    try {
                        URI replacementURI = new URI(uriStr);
                        openWebSocket(replacementURI);
                    } catch (URISyntaxException e) {
                        AlertDialog.Builder diaFailed = new AlertDialog.Builder(getApplicationContext());
                        diaFailed.setMessage("Bad URI");
                        diaFailed.setNeutralButton("OK", null);
                    }
                }
            }
        });

        diaBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        diaBuilder.setMessage("Enter new webSocket address");
        AlertDialog dia = diaBuilder.create();
        dia.show();
    }
}
