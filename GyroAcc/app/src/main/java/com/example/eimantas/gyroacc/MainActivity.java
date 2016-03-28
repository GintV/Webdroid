package com.example.eimantas.gyroacc;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

import java.util.Locale;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Locale.setDefault(Locale.US);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    public void openChat(View view) {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    public void openTest(View view) {
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }
}
