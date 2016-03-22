package com.example.eimantas.gyroacc;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


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

        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor acc = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sm.registerListener(new SensorEventListener() {
            static final float NS2S = 1.0f / 1000000000.0f;

            float[] gravity = null;

            float[] last_val_acc = null;
            float[] real_acc = null;

            float[] last_val_velo = null;
            float[] velocity = null;

            float[] position = null;
            long last_timestamp = 0;

            final float alpha = 0.6f;

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (last_val_acc == null) {
                    gravity = new float[3];
                    real_acc = new float[3];
                    last_val_acc = new float[3];

                    last_val_velo = new float[3];
                    velocity = new float[3];
                    position = new float[3];

                    gravity[0] = gravity[1] = gravity[2] = 0f;
                    last_val_acc[0] = last_val_acc[1] = last_val_acc[2] = 0f;
                    last_val_velo[0] = last_val_velo[1] = last_val_velo[2] = 0f;

                    last_timestamp = event.timestamp;
                } else {
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

                    real_acc[0] = event.values[0] - gravity[0];
                    real_acc[1] = event.values[1] - gravity[1];
                    real_acc[2] = event.values[2] - gravity[2];

                    changeText(real_acc[0], real_acc[1], real_acc[2]);

                    float dt = (event.timestamp - last_timestamp) * NS2S;

                    for (int index = 0; index < 3; index++) {
                        if (real_acc[index] > 0.2f) {
                            velocity[index] += (real_acc[index] + last_val_acc[index]) / 2 * dt;
                            position[index] += (velocity[index] + last_val_velo[index]) / 2 * dt;
                        }
                        else if (real_acc[index] > 3.0f) {
                            velocity[index] += (3.0f + last_val_acc[index]) / 2 * dt;
                            position[index] += (velocity[index] + last_val_velo[index]) / 2 * dt;
                        }

                       // position[index] += (velocity[index] + last_val_velo[index]) / 2 * dt;
                    }


                    change(velocity[0], velocity[1], velocity[2], position[0], position[1], position[2]);

                    System.arraycopy(real_acc, 0, last_val_acc, 0, 3);
                    System.arraycopy(velocity, 0, last_val_velo, 0, 3);
                    last_timestamp = event.timestamp;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, acc, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void changeText(float x, float y, float z) {
        textViewX.setText(String.format("%2.2f", x));
        textViewY.setText(String.format("%2.2f", y));
        textViewZ.setText(String.format("%2.2f", z));

        if (Float.compare(Float.parseFloat(textViewXMax.getText().toString()), x) > 0) {
            textViewXMax.setText((String.format("%2.2f", x)));
        }
        if (Float.compare(Float.parseFloat(textViewYMax.getText().toString()), y) > 0) {
            textViewYMax.setText((String.format("%2.2f", y)));
        }
        if (Float.compare(Float.parseFloat(textViewZMax.getText().toString()), z) > 0) {
            textViewZMax.setText((String.format("%2.2f", z)));
        }
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
