package citrusfresh.webdroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends FragmentActivity implements SetUpFragment.OnPlayerInfoChangeListener, LobbyFragment.OnLobbyInflatedListener {

    private final int SEND_RATE = 7;

    private static WebSocketControl webSocket;
    private final Object webSocketLock = new Object();
    private SensorManager sm;
    private SensorEventListener rotListener;
    private Sensor rotation;
    protected float[] rotationMatrix;
    protected PositionFromRotation positionFromRotation;
    protected boolean in;
    private static Timer timer;
    private String currentStatus;
    private boolean colorsReceived;
    private boolean playersReceived;
    private boolean initializedColor;


    private PlayerInfoFragment playerListFragment;

    private String[] allColors;
    private ArrayList<String> availableColors;
    private ArrayList<Data> allPlayers;
    private final Data thisPlayer = new Data();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ((TextView) findViewById(R.id.textViewWaiting)).setText(getResources().getString(R.string.waiting_connection));

        availableColors = new ArrayList<>();
        allPlayers = new ArrayList<>();
        colorsReceived = false;
        playersReceived = false;
        initializedColor = false;
        currentStatus = "";

        String sessionId = getIntent().getStringExtra("sessionId");
        thisPlayer.setSessionID(sessionId);
        timer = new Timer();
        rotationMatrix = new float[9];
        positionFromRotation = new PositionFromRotation();
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        rotation = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        rotListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                calculateRotation(event.values);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sm.registerListener(rotListener, rotation, SensorManager.SENSOR_DELAY_FASTEST);
        sm.unregisterListener(rotListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        sm.registerListener(rotListener, rotation, SensorManager.SENSOR_DELAY_FASTEST);
        if (webSocket == null || webSocket.getReadyState() == WebSocket.READYSTATE.CLOSED) {
            try {
                webSocket = new WebSocketControl(new URI(getString(R.string.testServer))) {
                    @Override
                    public void onMessage(String message) {
                        handleWebSocketMessage(message);
                    }

                    @Override
                    public void onOpen(ServerHandshake handshakedata) {
                        super.onOpen(handshakedata);
                        handleOnOpen();
                    }
                };
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        synchronized (webSocketLock) {
            if (webSocket != null && webSocket.getReadyState() != WebSocket.READYSTATE.OPEN) {
                webSocket.connect();
            }
        }
        if (webSocket != null && webSocket.getReadyState() == WebSocket.READYSTATE.OPEN) {
            ((TextView) findViewById(R.id.textViewWaiting)).setText("");
            if (thisPlayer.getPlayerIsReady()) {
                startTimerTask();
            }
        }
        if(currentStatus.equals("lobby")) {
            switchToLobby();
        }
        Log.i("Websocket", currentStatus);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        sm.unregisterListener(rotListener);
        thisPlayer.setPlayerIsReady(false);
        timer.cancel();
        timer.purge();
        super.onPause();
    }

    @Override
    public void onStop() {
        timer.cancel();
        timer.purge();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        timer.purge();
        webSocket.close();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(Html.fromHtml("<font color='#deddd6'>Do You really want to quit this session?</font>"))
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        pressBack();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                }).show();
    }

    public void pressBack() {
        synchronized (webSocketLock) {
            webSocket.close();
        }
        super.onBackPressed();
    }

    @Override
    public void onPlayerInfoChange(String name, String initials, String color, boolean isReady, boolean isCalibrating) {
        thisPlayer.setPlayerName(name);
        thisPlayer.setPlayerInitials(initials);
        thisPlayer.setPlayerColor(color);
        thisPlayer.setPlayerIsReady(isReady);
        thisPlayer.setPlayerIsCalibrating(isCalibrating);

        if (!isCalibrating) {
            if (isReady) {
                in = false;
            }
            Packet toSend = new Packet(Packet.TYPE_PLAYER_INFO_CHANGE, thisPlayer.getPlayerInfoChange());
            String data = toSend.toJSON();
            if (webSocket.isConnected()) {
                synchronized (webSocketLock) {
                    webSocket.send(data);
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (thisPlayer.getPlayerIsReady()) {
                        startTimerTask();
                    } else {
                        timer.cancel();
                    }
                }
            });
        } else {
            in = false;
        }
    }

    @Override
    public ArrayList<String> getAvailableColors() {
        return availableColors;
    }

    @Override
    public void onColorChange(String color) {
        thisPlayer.setPlayerColor(color);
        Packet toSend = new Packet(Packet.TYPE_PLAYER_INFO_CHANGE, thisPlayer.getPlayerInfoChange());
        String data = toSend.toJSON();
        if (webSocket.isConnected()) {
            synchronized (webSocketLock) {
                webSocket.send(data);
            }
        }
    }

    @Override
    public Data.PlayerInfoChange getPlayerInfo() {
        return thisPlayer.getPlayerInfoChange();
    }

    private void calculateRotation(float[] values) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values);

        if (!in) {
            positionFromRotation.calibrate(rotationMatrix);
            in = true;
        }

        positionFromRotation.processRotation(rotationMatrix);
        thisPlayer.setPlayerPosition(positionFromRotation.getXCoordinateMonitor(), positionFromRotation.getYCoordinateMonitor());
    }

    private void startTimerTask() {
        timer.cancel();
        timer.purge();
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    String data = new Packet(Packet.TYPE_PLAYER_POSITION, thisPlayer.getPlayerPosition()).toJSON();
                    if (data != null) {
                        if (webSocket.isConnected()) {
                            synchronized (webSocketLock) {
                                webSocket.send(data);
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 0, SEND_RATE);
    }

    private void handleWebSocketMessage(String message) {
        Log.i("Websocket", message);
        Packet received = Packet.fromJSON(message);
        if (received != null) {
            ObjectMapper mapper = new ObjectMapper();
            int index;
            String dataPart;
            switch (received.getType()) {
                case "error":
                    index = message.indexOf("\"data\":");
                    dataPart = message.substring(index + 7, message.length() - 1);
                    try {
                        final ErrorMessage err = mapper.readValue(dataPart, ErrorMessage.class);
                        Runnable showToast = new Runnable() {
                            @Override
                            public void run() {
                                Toast toast = Toast.makeText(getApplicationContext(), err.getErrorText(), Toast.LENGTH_LONG);
                                toast.show();
                                finish();
                            }
                        };
                        synchronized (webSocketLock) {
                            webSocket.close();
                        }
                        runOnUiThread(showToast);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "colors":
                    index = message.indexOf("\"data\":");
                    dataPart = message.substring(index + 7, message.length() - 1);
                    try {
                        allColors = mapper.readValue(dataPart, String[].class);
                        setAvailableColors();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) findViewById(R.id.textViewWaiting)).setText("");
                            }
                        });
                        colorsReceived = true;
                        if (!initializedColor && playersReceived) {
                            initializedColor = true;
                            thisPlayer.setPlayerColor(availableColors.get(0));
                            Packet toSend = new Packet(Packet.TYPE_PLAYER_INFO_CHANGE, thisPlayer.getPlayerInfoChange());
                            String data = toSend.toJSON();
                            if (webSocket.isConnected()) {
                                synchronized (webSocketLock) {
                                    webSocket.send(data);
                                }
                            }
                            switchToLobby();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "gameInfo":
                    index = message.indexOf("\"data\":");
                    dataPart = message.substring(index + 7, message.length() - 1);
                    int indexStart = dataPart.indexOf("\"status\":");
                    index = dataPart.indexOf("\",");
                    String status = dataPart.substring(indexStart + 10, index);
                    Log.i("Websocket", status);
                    if (!status.equals(currentStatus)) {
                        switch (status) {
                            case "lobby":
                                break;
                        }
                    }
                    currentStatus = status;
                    indexStart = index + 12;
                    index = dataPart.indexOf("]}");
                    String playerArrayJSON = dataPart.substring(indexStart, index + 1);
                    Log.i("Websocket", playerArrayJSON);
                    try {
                        Player[] players = mapper.readValue(playerArrayJSON, Player[].class);
                        allPlayers.clear();
                        for (Player p : players) {
                            allPlayers.add(new Data(p.getName(), p.getInitials(), p.getColor(), p.isReady()));
                        }
                        setAvailableColors();
                        if (playerListFragment != null) {
                            playerListFragment.setPlayers(allPlayers);
                        }
                        playersReceived = true;
                        if (!initializedColor && colorsReceived) {
                            initializedColor = true;
                            thisPlayer.setPlayerColor(availableColors.get(0));
                            Packet toSend = new Packet(Packet.TYPE_PLAYER_INFO_CHANGE, thisPlayer.getPlayerInfoChange());
                            String data = toSend.toJSON();
                            if (webSocket.isConnected()) {
                                synchronized (webSocketLock) {
                                    webSocket.send(data);
                                }
                            }
                            switchToLobby();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;

            }
        }
    }

    private void handleOnOpen() {
        Packet toSend = new Packet();
        toSend.setType(Packet.TYPE_NEW_CONNECTION);
        toSend.setData(thisPlayer.getNewConnection());
        String data = toSend.toJSON();
        if (data != null) {
            synchronized (webSocketLock) {
                webSocket.send(data);
            }
        }
    }

    private void switchToGame() {
        playerListFragment = null;
        makeTransaction(InGameFragment.newInstance(thisPlayer.getPlayerInfoChange().getPlayerColor()), "game");
    }

    private void switchToLobby() {
        LobbyFragment lobbyFragment = new LobbyFragment();
        makeTransaction(lobbyFragment, "lobby");
    }

    private void makeTransaction(Fragment fragment, String tag) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(R.id.fragment_container_game, fragment, tag);

        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onLobbyInflated() {
        playerListFragment = (PlayerInfoFragment) getSupportFragmentManager().findFragmentByTag("lobby").getChildFragmentManager().findFragmentById(R.id.detail);
        if (playerListFragment != null) {
            playerListFragment.setPlayers(allPlayers);
        }
    }

    private void setAvailableColors() {
        availableColors.clear();
        for (String c : allColors) {
            boolean found = false;
            for (Data d : allPlayers) {
                if (d.getPlayerInfoChange().getPlayerColor().equals(c) &&
                        !thisPlayer.getPlayerInfoChange().getPlayerColor().equals(c)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                availableColors.add(c);
            }
        }
    }
}
