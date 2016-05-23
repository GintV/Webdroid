package citrusfresh.webdroid;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.util.Log;

import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class GameActivity extends FragmentActivity implements SetUpFragment.OnPlayerInfoChangeListener {

    private final int SEND_RATE = 10;

    private static WebSocketControl webSocket;
    private final Object webSocketLock = new Object();
    private SensorManager sm;
    private SensorEventListener rotListener;
    private Sensor rotation;
    protected float[] rotationMatrix;
    protected PositionFromRotation positionFromRotation;
    protected boolean in;
    private static Timer timer;
    private boolean firstConnect;

    private String[] allColors;
    private ArrayList<String> availableColors;
    private Data[] allPlayers;
    private final Data thisPlayer = new Data();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        availableColors = new ArrayList<>();
        availableColors.add("#fdb913");
        availableColors.add("#006a44");
        availableColors.add("#c1272d");

        String sessionId = getIntent().getStringExtra("sessionId");
        thisPlayer.setSessionID(sessionId);
        firstConnect = true;
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

        if (findViewById(R.id.fragment_container_game) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            LobbyFragment firstFragment = new LobbyFragment();

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container_game, firstFragment).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO inicializuoti web socketa onCreate, onResume palikti tik connect
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
            synchronized (webSocketLock) {
                webSocket.connect();
            }
            Thread.sleep(500);
            if (!webSocket.isConnected()) {
                webSocket = new WebSocketControl(new URI(getString(R.string.mainServer))) {
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
                synchronized (webSocketLock) {
                    webSocket.connect();
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sm.registerListener(rotListener, rotation, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        sm.unregisterListener(rotListener);
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
    }

    // TODO siusti zaidejo ID, jei ne pirmas connection
    private void handleOnOpen() {
        if (firstConnect) {
            firstConnect = false;
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
        if (thisPlayer.getPlayerIsReady()) {
            startTimerTask();
        }
    }
}
