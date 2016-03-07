package com.example.eimantas.gyroacc;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

public class CalibrateActivity extends AppCompatActivity {

    private TextView textViewX;
    private TextView textViewY;
    private TextView textViewZ;
    private SensorManager sm;
    private SensorEventListener accListener;
    private Timer webSocketTimer;
    private WebSocketControl webSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);

        webSocketTimer = new Timer();

        try {
            webSocket = new WebSocketControl(new URI("ws://218.gaikaz.tk:3000"), (TextView) findViewById(R.id.textViewCnt));
            webSocket.connect();
            webSocketTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        webSocket.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);
                    }
                    catch (Exception ex) {

                    }
                }
            }, 0, 34);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        textViewX = (TextView) findViewById(R.id.textViewCalibX);
        textViewY = (TextView) findViewById(R.id.textViewCalibY);
        textViewZ = (TextView) findViewById(R.id.textViewCalibZ);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accListener = new SensorEventListener() {

            static final float NS2S = 1.0f / 1000000000.0f;
            float[] last_values = null;
            float[] velocity = null;
            float[] position = null;
            float[] gravity = null;
            float[] real_acc = null;
            long last_timestamp = 0;

            // alpha is calculated as t / (t + dT)
            // with t, the low-pass filter's time-constant
            // and dT, the event delivery rate

            final float alpha = 0.999f;


            @Override
            public void onSensorChanged(SensorEvent event) {

                if (last_values != null) {
                    float dt = (event.timestamp - last_timestamp) * NS2S;

                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                    real_acc[0] = event.values[0] - gravity[0];
                    real_acc[1] = event.values[1] - gravity[1];
                    real_acc[2] = event.values[2] - gravity[2];

                    for (int index = 0; index < 3; ++index) {
                        velocity[index] += (real_acc[index] + last_values[index]) / 2 * dt;
                        position[index] += velocity[index] * dt;
                    }
                } else {
                    gravity = new float[3];
                    real_acc = new float[3];

                    last_values = new float[3];
                    velocity = new float[3];
                    position = new float[3];

                    gravity[0] = gravity[1] = gravity[2] = 0f;
                    velocity[0] = velocity[1] = velocity[2] = 0f;
                    position[0] = position[1] = position[2] = 0f;
                }
                changeText(position[0], position[1], position[2]);


                System.arraycopy(event.values, 0, last_values, 0, 3);
                last_timestamp = event.timestamp;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        sm.registerListener(accListener, acc, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sm.unregisterListener(accListener);
        webSocketTimer.cancel();
        webSocket.close();
    }

    protected void changeText(float x, float y, float z) {
        textViewX.setText(String.format("%2.2f", x));
        textViewY.setText(String.format("%2.2f", y));
        textViewZ.setText(String.format("%2.2f", z));
    }
}
