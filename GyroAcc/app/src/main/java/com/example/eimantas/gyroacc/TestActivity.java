package com.example.eimantas.gyroacc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TestActivity extends Activity {

    private static long SEND_RATE = 34;
    private static float DRIFT_COMPENSATION = 0.001f;
    private static float MIN_SIGNIFICANT_VALUE = 0.005f;

    private TextView textViewPosX;
    private TextView textViewPosY;
    private TextView textViewPosZ;
    private TextView textViewRotX;
    private TextView textViewRotY;
    private TextView textViewRotZ;

    private SensorManager sm;
    private SensorEventListener accListener, rotListener;
    private Sensor acc, rot;
    private final Object accLock = new Object();
    private final Object rotLock = new Object();
    protected float[] accValues;
    protected float[] rotValues;
    protected int accCount;
    protected int rotCount;
    protected float[] currSpeed;
    protected float[] currPos;
    protected float[] currRot;
    protected Timer timer;
    protected WebSocketControl webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        textViewPosX = (TextView) findViewById(R.id.textViewPosX);
        textViewPosY = (TextView) findViewById(R.id.textViewPosY);
        textViewPosZ = (TextView) findViewById(R.id.textViewPosZ);
        textViewRotX = (TextView) findViewById(R.id.textViewRotX);
        textViewRotY = (TextView) findViewById(R.id.textViewRotY);
        textViewRotZ = (TextView) findViewById(R.id.textViewRotZ);
        accValues = new float[3];
        rotValues = new float[3];
        reset(null);
        timer = new Timer();
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        acc = sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        rot = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                synchronized (accLock) {
                    for (int i = 0; i < 3; i++) {
                        accValues[i] += event.values[i];
                    }
                    accCount++;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        rotListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                synchronized (rotLock) {
                    for (int i = 0; i < 3; i++) {
                        rotValues[i] += event.values[i];
                    }
                    rotCount++;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
    }

    protected void calculateAccData() {
        float[] accData = new float[3];
        int accCnt;
        synchronized (accLock) {
            System.arraycopy(accValues, 0, accData, 0, 3);
            accCnt = accCount;
            accCount = 0;
            for (int i = 0; i < 3; i++) {
                accValues[i] = 0f;
            }
        }
        if (accCnt > 0) {
            for (int i = 0; i < 3; i++) {
                if (Float.compare(Math.abs(accData[i]), MIN_SIGNIFICANT_VALUE) < 0) {
                    accData[i] = 0f;
                }
                currSpeed[i] += accData[i] / accCnt * SEND_RATE / 1000;
                float drift = accCnt * DRIFT_COMPENSATION;
                if (Float.compare(Math.abs(currSpeed[i]), drift) > 0) {
                    if (Float.compare(currSpeed[i], 0f) > 0) {
                        currSpeed[i] -= drift;
                    } else {
                        currSpeed[i] += drift;
                    }
                } else {
                    currSpeed[i] = 0f;
                }
                if (Float.compare(Math.abs(currSpeed[i]), MIN_SIGNIFICANT_VALUE) > 0) {
                    currPos[i] += currSpeed[i] * SEND_RATE / 1000;
                }
            }
        }
    }

    protected void calculateRotData() {
        float[] rotData = new float[3];
        int rotCnt;
        synchronized (rotLock) {
            System.arraycopy(rotValues, 0, rotData, 0, 3);
            rotCnt = rotCount;
            rotCount = 0;
            for (int i = 0; i < 3; i++) {
                rotValues[i] = 0;
            }
        }
        System.arraycopy(rotData, 0, currRot, 0, 3);
    }

    protected String formatData() {
        return "";
    }

    protected void sendData(String data) {
        if (webSocket != null) {
            try {
                webSocket.send(data);
            } catch (Exception e) {

            }
        }
    }

    protected void showData() {
        textViewPosX.setText(String.format(Locale.US, "%2.2f", currPos[0]));
        textViewPosY.setText(String.format(Locale.US, "%2.2f", currPos[1]));
        textViewPosZ.setText(String.format(Locale.US, "%2.2f", currPos[2]));
        textViewRotX.setText(String.format(Locale.US, "%2.2f", currRot[0]));
        textViewRotY.setText(String.format(Locale.US, "%2.2f", currRot[1]));
        textViewRotZ.setText(String.format(Locale.US, "%2.2f", currRot[2]));
    }

    public void reset(View view) {
        currSpeed = new float[3];
        currPos = new float[3];
        currRot = new float[3];
        showData();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            webSocket = new WebSocketControl(new URI("ws://218.gaikaz.tk:80"));
            webSocket.connect();
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    calculateAccData();
                    calculateRotData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showData();
                        }
                    });
                    sendData(formatData());
                }

            }, 0, SEND_RATE);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        sm.registerListener(accListener, acc, SensorManager.SENSOR_DELAY_FASTEST);
        sm.registerListener(rotListener, rot, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        super.onPause();
        sm.unregisterListener(rotListener);
        sm.unregisterListener(accListener);
        timer.cancel();
    }

    @Override
    public void onStop() {
        super.onStop();
        webSocket.close();
    }
}
