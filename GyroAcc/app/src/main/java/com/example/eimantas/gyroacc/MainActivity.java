package com.example.eimantas.gyroacc;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SensorManager sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void openAcc(View view) {
        Intent intent = new Intent(this, AccActivity.class);
        startActivity(intent);
    }

    public void openGyro(View view) {
        Intent intent = new Intent(this, GyroActivity.class);
        startActivity(intent);
    }

    public void openCalib(View view) {
        Intent intent = new Intent(this, CalibrateActivity.class);
        startActivity(intent);
    }
}
