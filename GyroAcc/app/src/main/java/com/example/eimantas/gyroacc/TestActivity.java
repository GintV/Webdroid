package com.example.eimantas.gyroacc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class TestActivity extends Activity {

    private static long SEND_RATE = 5;
    private static float DRIFT_COMPENSATION = 0.001f;
    private static float MIN_SIGNIFICANT_VALUE = 0.005f;

    private TextView textViewPosX;
    private TextView textViewPosY;
    private TextView textViewPosZ;
    private TextView textViewRotX;
    private TextView textViewRotY;
    private TextView textViewRotZ;
    private TextView textViewScrX;
    private TextView textViewScrY;

    private SensorManager sm;
    private SensorEventListener accListener, rotListener;
    private Sensor acc, rot;
    private final Object accLock = new Object();
    private final Object rotLock = new Object();
    protected float[] accValues;
    protected float[] rotValues;
    protected int accCount;
    protected float[] currSpeed;
    protected float[] currPos;
    protected float[] currRot;
    protected float[] directionVector;
    protected Coordinates coordinates;
    boolean rotInit;
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
        textViewScrX = (TextView) findViewById(R.id.textViewScreenX);
        textViewScrY = (TextView) findViewById(R.id.textViewScreenY);
        accValues = new float[3];
        rotValues = new float[4];
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
                    System.arraycopy(event.values, 0, rotValues, 0, 4);
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
        float[] rotData = new float[4];
        synchronized (rotLock) {
            System.arraycopy(rotValues, 0, rotData, 0, 4);
        }

        if (rotInit) {
            float[] rotationQuat = multiplyQuat(rotData, invertQuat(currRot));
            directionVector = multiplyQuat(rotationQuat, directionVector);
            directionVector = multiplyQuat(directionVector, invertQuat(rotationQuat));
            directionVector[3] = 0f;
            if (directionVector[2] != 0) {
                float t = 1 / directionVector[2];
                float[] tempCoordinates = new float[2];
                for (int i = 0; i < 2; i++) {
                    tempCoordinates[i] = t * directionVector[i];
                    if (tempCoordinates[i] > 1f)
                        tempCoordinates[i] = 1f;
                    else if (tempCoordinates[i] < -1f)
                        tempCoordinates[i] = -1f;
                }
                coordinates.setX(-tempCoordinates[0]);
                coordinates.setY(tempCoordinates[1]);
            }
        }
        else {
            directionVector[2] = 1f;
            rotInit = true;
        }
        System.arraycopy(rotData, 0, currRot, 0, 4);
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
        textViewRotX.setText(String.format(Locale.US, "%2.2f", directionVector[0]));
        textViewRotY.setText(String.format(Locale.US, "%2.2f", directionVector[1]));
        textViewRotZ.setText(String.format(Locale.US, "%2.2f", directionVector[2]));
        textViewScrX.setText(String.format(Locale.US, "%2.2f", coordinates.getX()));
        textViewScrY.setText(String.format(Locale.US, "%2.2f", coordinates.getY()));
    }

    public void reset(View view) {
        currSpeed = new float[3];
        currPos = new float[3];
        currRot = new float[4];
        coordinates = new Coordinates();
        directionVector = new float[4];
        rotInit = false;
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
                    calculateRotData();
                    calculateAccData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showData();
                        }
                    });
                    String dataToSend = coordinates.toJSON();
                    if (dataToSend != null)
                        sendData(dataToSend);
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

    protected float[] multiplyQuat (float[] q1, float[] q2) {
        float[] answer = new float[4];

        answer[0] = q1[3] * q2[0] + q1[0] * q2[3] - q1[1] * q2[2] + q1[2] * q2[1];
        answer[1] = q1[3] * q2[1] + q1[0] * q2[2] + q1[1] * q2[3] - q1[2] * q2[0];
        answer[2] = q1[3] * q2[2] - q1[0] * q2[1] + q1[1] * q2[0] + q1[2] * q2[3];
        answer[3] = q1[3] * q2[3] - q1[0] * q2[0] - q1[1] * q2[1] - q1[2] * q2[2];

        return answer;
    }

    protected float[] invertQuat(float[] q) {
        float[] answer = new float[4];

        answer[3] = q[3];
        for (int i = 0; i < 3; i++) {
            answer[i] = -q[i];
        }

        return answer;
    }

    protected class Coordinates implements Data {

        private float x;
        private float y;

        Coordinates() {
            x = 0f;
            y = 0f;
        }

        public void setY(float y) {
            this.y = y;
        }

        public void setX(float x) {
            this.x = x;
        }

        public double getY() {

            return y;
        }

        public double getX() {

            return x;
        }

        @Override
        public String toJSON() {
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonString = null;

            try {
                jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            return jsonString;
        }
    }
}
