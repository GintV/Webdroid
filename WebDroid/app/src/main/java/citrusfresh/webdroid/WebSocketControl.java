package citrusfresh.webdroid;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

/**
 * Created by Eimantas on 2016-03-07.
 */
public class WebSocketControl extends WebSocketClient {

    private boolean connected;

    public WebSocketControl(URI serverURI) {
        super(serverURI);
        connected = false;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.i("Websocket", "Opened");
        connected = true;
    }

    @Override
    public void onMessage(String message) {

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.i("Websocket", "Closed " + reason);
        connected = false;
    }

    @Override
    public void onError(Exception ex) {
        Log.i("Websocket", "Error " + ex.getMessage());
    }

    public boolean isConnected() {
        return connected;
    }
}
