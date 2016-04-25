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
import java.util.Timer;
import java.util.TimerTask;

public class AccActivity extends Activity {

    private TextView textViewX;
    private TextView textViewY;
    private TextView textViewZ;
    private TextView textViewXMax;
    private TextView textViewYMax;
    private TextView textViewZMax;

    private TextView textSpeedX;
    private TextView textSpeedY;
    private TextView textSpeedZ;

    private TextView textPosX;
    private TextView textPosY;
    private TextView textPosZ;


    private Timer webSocketTimer;
    private WebSocketControl webSocket;
    protected boolean in;

    private Sensor acc;
    private SensorManager sm;
    private SensorEventListener accListener;

    protected LinearPosition linearPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc);
        textViewX = (TextView)findViewById(R.id.textViewAccX);
        textViewY = (TextView)findViewById(R.id.textViewAccY);
        textViewZ = (TextView)findViewById(R.id.textViewAccZ);
        textViewXMax = (TextView)findViewById(R.id.textViewAccXMax);
        textViewYMax = (TextView)findViewById(R.id.textViewAccYMax);
        textViewZMax = (TextView)findViewById(R.id.textViewAccZMax);

        textSpeedX = (TextView) findViewById(R.id. textViewSpeedX);
        textSpeedY = (TextView) findViewById(R.id. textViewSpeedY);
        textSpeedZ = (TextView) findViewById(R.id. textViewSpeedZ);

        textPosX = (TextView) findViewById(R.id.textViewPosX);
        textPosY = (TextView) findViewById(R.id.textViewPosY);
        textPosZ = (TextView) findViewById(R.id.textViewPosZ);

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sm.registerListener(new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                //SensorManager.getRotationMatrixFromVector(rotationMatrix, values);

                if(!in) {
                    linearPosition = new LinearPosition(event.timestamp);
                    in = true;
                }

                linearPosition.processLinearMotion(event.values[0], event.values[1], event.values[2], event.timestamp);
                //positionFromRotation.processRotation(rotationMatrix);

                //changeText(positionFromRotation.getXCoordinateMonitor(), -positionFromRotation.getYCoordinateMonitor(), positionFromRotation.getPointerOrientation());
                linearPosition.toJSON();

                changeText((float) linearPosition.getLinearMotion(), (float) linearPosition.getCurrentSpeed(), (float) linearPosition.getLinearAcceleration());
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, acc, SensorManager.SENSOR_DELAY_FASTEST);
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
                        String data = linearPosition.toJSON();
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

    protected void changeText(float x, float y, float z) {
        textViewX.setText(String.format("%2.2f", x));
        textViewY.setText(String.format("%2.2f", y));
        textViewZ.setText(String.format("%2.2f", z));

        /*
        if (Float.compare(Float.parseFloat(textViewXMax.getText().toString()), x) > 0) {
            textViewXMax.setText((String.format("%2.2f", x)));
        }
        if (Float.compare(Float.parseFloat(textViewYMax.getText().toString()), y) > 0) {
            textViewYMax.setText((String.format("%2.2f", y)));
        }
        if (Float.compare(Float.parseFloat(textViewZMax.getText().toString()), z) > 0) {
            textViewZMax.setText((String.format("%2.2f", z)));
        }
        */
    }

    protected void change(float speedX, float speedY, float speedZ, float posX, float posY, float posZ) {
        textSpeedX.setText(String.format("%2.2f", speedX));
        textSpeedY.setText(String.format("%2.2f", speedY));
        textSpeedZ.setText(String.format("%2.2f", speedZ));

        textPosX.setText(String.format("%2.2f", posX));
        textPosY.setText(String.format("%2.2f", posY));
        textPosZ.setText(String.format("%2.2f", posZ));
    }

    public void resetMax(View view) {
        textViewXMax.setText("0.00");
        textViewYMax.setText("0.00");
        textViewZMax.setText("0.00");

        //textSpeedX.setText("0.00");
        //textSpeedY.setText("0.00");
        //textSpeedZ.setText("0.00");


    }

}
