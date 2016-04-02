package com.example.eimantas.gyroacc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
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
    protected float[] I;
    protected float[] O;
    private Timer webSocketTimer;
    private WebSocketControl webSocket;

    private TextView L1;
    private TextView L2;
    private TextView L3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);

        I = new float[9];
        O = new float[9];
        in = false;

        webSocketTimer = new Timer();

        /*try {
            webSocket = new WebSocketControl(new URI("ws://218.gaikaz.tk:80"), (TextView) findViewById(R.id.textViewCnt));
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
        }*/

        L1 = (TextView) findViewById(R.id.textView35);
        L2 = (TextView) findViewById(R.id.textView36);
        L3 = (TextView) findViewById(R.id.textView37);


        textViewX = (TextView) findViewById(R.id.textViewCalibX);
        textViewY = (TextView) findViewById(R.id.textViewCalibY);
        textViewZ = (TextView) findViewById(R.id.textViewCalibZ);
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        acc = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accListener = new SensorEventListener() {

            //  static final float NS2S = 1.0f / 1000000000.0f;
            //  float[] last_values = null;
            //  float[] velocity = null;
            //  float[] position = null;
            // float[] gravity = null;
            // float[] real_acc = null;
            // long last_timestamp = 0;

            // alpha is calculated as t / (t + dT)
            // with t, the low-pass filter's time-constant
            // and dT, the event delivery rate

            //final float alpha = 0.999f;


            @Override
            public void onSensorChanged(SensorEvent event) {

                calculateRotation(event.values);

                /*if (last_values != null) {
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
                }*/





                //float x = R[0]*event.values[0] + R[1]*event.values[1] + R[2]*event.values[2];
                //float y = R[3]*event.values[0] + R[4]*event.values[1] + R[5]*event.values[2];
                //float z = R[6]*event.values[0] + R[7]*event.values[1] + R[8]*event.values[2];





                //System.arraycopy(event.values, 0, last_values, 0, 3);
                //last_timestamp = event.timestamp;
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };


        sm.registerListener(accListener, acc, 30);
    }

    protected void calculateRotation(float[] values) {
        float[] R = new float[9];
        SensorManager.getRotationMatrixFromVector(R, values);

        if (!in) {
            in = true;

            float det = R[0] * R[4] * R[8] + R[2] * R[3] * R[7] + R[1] * R[5] * R[6] -
                    R[2] * R[4] * R[6] - R[1] * R[3] * R[8] - R[0] * R[5] * R[7];

            I[0] = (R[4] * R[8] - R[5] * R[7]) / det;
            I[1] = (R[2] * R[7] - R[1] * R[8]) / det;
            I[2] = (R[1] * R[5] - R[2] * R[4]) / det;
            I[3] = (R[5] * R[6] - R[3] * R[8]) / det;
            I[4] = (R[0] * R[8] - R[2] * R[6]) / det;
            I[5] = (R[2] * R[3] - R[0] * R[5]) / det;
            I[6] = (R[3] * R[7] - R[4] * R[6]) / det;
            I[7] = (R[1] * R[6] - R[0] * R[7]) / det;
            I[8] = (R[0] * R[4] - R[1] * R[3]) / det;

        }

        O[0] = I[0] * R[0] + I[1] * R[3] + I[2] * R[6];
        O[1] = I[0] * R[1] + I[1] * R[4] + I[2] * R[7];
        O[2] = I[0] * R[2] + I[1] * R[5] + I[2] * R[8];

        O[3] = I[3] * R[0] + I[4] * R[3] + I[5] * R[6];
        O[4] = I[3] * R[1] + I[4] * R[4] + I[5] * R[7];
        O[5] = I[3] * R[2] + I[4] * R[5] + I[5] * R[8];

        O[6] = I[6] * R[0] + I[7] * R[3] + I[8] * R[6];
        O[7] = I[6] * R[1] + I[7] * R[4] + I[8] * R[7];
        O[8] = I[6] * R[2] + I[7] * R[5] + I[8] * R[8];


        double yawn = Math.atan2(O[3], O[0]) * 180 / Math.PI;
        double pitch = Math.atan2(O[7], O[8]) * 180 / Math.PI;
        double roll = Math.atan2(-O[6], Math.sqrt(Math.pow(O[7], 2) + Math.pow(O[8], 2))) * 180 / Math.PI;


        String l1 = String.format("%2.2f %2.2f %2.2f", O[0], O[1], O[2]);
        String l2 = String.format("%2.2f %2.2f %2.2f", O[3], O[4], O[5]);
        String l3 = String.format("%2.2f %2.2f %2.2f", O[6], O[7], O[8]);

        changeR(l1, l2, l3);
        changeText(yawn, pitch, roll);
    }

    public void calibrate(View view) {
        in = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        sm.unregisterListener(accListener);
        //webSocketTimer.cancel();
        //webSocket.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sm.registerListener(accListener, acc, 30);
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
