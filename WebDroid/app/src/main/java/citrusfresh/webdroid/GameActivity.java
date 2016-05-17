package citrusfresh.webdroid;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
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
    private Data thisPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

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
        String sessionId = getIntent().getStringExtra("sessionId");
        thisPlayer = new Data();
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
    }

    @Override
    public void onResume() {
        // TODO inicializuoti web socketa onCreate, onResume palikti tik connect
        try {
            webSocket = new WebSocketControl(new URI(getString(R.string.testServer))) {
                @Override
                public void onMessage(String message) {
                    handleWebSocketMessage(message);
                }
            };
            synchronized (webSocketLock) {
                webSocket.connect();
            }
            Thread.sleep(50);
            if (!webSocket.isConnected()) {
                webSocket = new WebSocketControl(new URI(getString(R.string.mainServer))) {
                    @Override
                    public void onMessage(String message) {
                        handleWebSocketMessage(message);
                    }
                };
                synchronized (webSocketLock) {
                    webSocket.connect();
                }
                Thread.sleep(50);
            }
            if (firstConnect) {
                Packet toSend = new Packet();
                firstConnect = false;
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
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        sm.registerListener(rotListener, rotation, SensorManager.SENSOR_DELAY_FASTEST);
        super.onResume();
    }

    @Override
    public void onPause() {
        sm.unregisterListener(rotListener);
        timer.cancel();
        super.onPause();
    }

    @Override
    public void onStop() {
        timer.cancel();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        webSocket.close();
        super.onDestroy();
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
            synchronized (webSocketLock) {
                webSocket.send(data);
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
        }
        else {
            in = false;
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
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    String data = new Packet(Packet.TYPE_PLAYER_POSITION, thisPlayer.getPlayerPosition()).toJSON();
                    if (data != null) {
                        synchronized (webSocketLock) {
                            webSocket.send(data);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }, 0, SEND_RATE);
    }

    private void handleWebSocketMessage(String message) {

    }
}
