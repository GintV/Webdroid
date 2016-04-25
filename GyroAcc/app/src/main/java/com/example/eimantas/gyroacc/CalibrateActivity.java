package com.example.eimantas.gyroacc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class CalibrateActivity extends Activity {

    private TextView textViewX;
    private TextView textViewY;
    private TextView textViewZ;
    private Sensor acc;
    private SensorManager sm;
    private SensorEventListener accListener;
    protected boolean in;
    protected float[] rotationMatrix;
    protected PositionFromRotation positionFromRotation;
    private Timer webSocketTimer;
    private WebSocketControl webSocket;

    private TextView L1;
    private TextView L2;
    private TextView L3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        rotationMatrix = new float[9];
        positionFromRotation = new PositionFromRotation();
        in = false;

        L1 = (TextView) findViewById(R.id.textView35);
        L2 = (TextView) findViewById(R.id.textView36);
        L3 = (TextView) findViewById(R.id.textView37);


        textViewX = (TextView) findViewById(R.id.textViewCalibX);
        textViewY = (TextView) findViewById(R.id.textViewCalibY);
        textViewZ = (TextView) findViewById(R.id.textViewCalibZ);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        acc = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accListener = new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                calculateRotation(event.values);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sm.registerListener(accListener, acc, 30000);
    }

    protected void calculateRotation(float[] values) {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, values);

        if(!in) {
            positionFromRotation.calibrate(rotationMatrix);
            in = true;
        }

        positionFromRotation.processRotation(rotationMatrix);

        changeText(positionFromRotation.getXCoordinateMonitor(), -positionFromRotation.getYCoordinateMonitor(), positionFromRotation.getPointerOrientation());
        positionFromRotation.toJSON();
    }

    public void calibrate(View view) {
        in = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        sm.unregisterListener(accListener);
        webSocketTimer.cancel();
        webSocket.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sm.registerListener(accListener, acc, SensorManager.SENSOR_DELAY_FASTEST);

        webSocketTimer = new Timer();

        try {
            webSocket = new WebSocketControl(new URI("ws://218.gaikaz.tk:80"));
            webSocket.connect();
            webSocketTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        String data = positionFromRotation.toJSON();
                        if (data != null) {
                            webSocket.send(data);
                        }
                    }
                    catch (Exception ex) {

                    }
                }
            }, 0, 5);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    protected void changeText(double x, double y, double z) {
        textViewX.setText(String.format("%2.2f", x));
        textViewY.setText(String.format("%2.2f", y));
        textViewZ.setText(String.format("%2.2f", z));
    }

    protected void changeR(String l1, String l2, String l3) {
        L1.setText(l1);
        L2.setText(l2);
        L3.setText(l3);
    }
}
