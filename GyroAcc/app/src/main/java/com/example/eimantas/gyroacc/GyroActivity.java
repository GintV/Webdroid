package com.example.eimantas.gyroacc;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class GyroActivity extends AppCompatActivity {

    private TextView textViewX;
    private TextView textViewY;
    private TextView textViewZ;
    private TextView textViewXMax;
    private TextView textViewYMax;
    private TextView textViewZMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyro);
        textViewX = (TextView)findViewById(R.id.textViewGyroX);
        textViewY = (TextView)findViewById(R.id.textViewGyroY);
        textViewZ = (TextView)findViewById(R.id.textViewGyroZ);
        textViewXMax = (TextView)findViewById(R.id.textViewGyroXMax);
        textViewYMax = (TextView)findViewById(R.id.textViewGyroYMax);
        textViewZMax = (TextView)findViewById(R.id.textViewGyroZMax);
        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        Sensor gyr = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sm.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                changeText(event.values[0], event.values[1], event.values[2]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, gyr, SensorManager.SENSOR_DELAY_FASTEST);
    }

    protected void changeText(float x, float y, float z) {
        textViewX.setText(String.format("%2.2f", x));
        textViewY.setText(String.format("%2.2f", y));
        textViewZ.setText(String.format("%2.2f", z));

        if (Float.compare(Float.parseFloat(textViewXMax.getText().toString()), x) < 0) {
            textViewXMax.setText((String.format("%2.2f", x)));
        }
        if (Float.compare(Float.parseFloat(textViewYMax.getText().toString()), y) < 0) {
            textViewYMax.setText((String.format("%2.2f", y)));
        }
        if (Float.compare(Float.parseFloat(textViewZMax.getText().toString()), z) < 0) {
            textViewZMax.setText((String.format("%2.2f", z)));
        }
    }

    public void resetMax(View view) {
        textViewXMax.setText("0.00");
        textViewYMax.setText("0.00");
        textViewZMax.setText("0.00");
    }
}
