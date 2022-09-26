package com.rumble.rumble;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FallActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private float x=0, y=0, z=0;

    private TextView textViewValue, textViewFall;
    private Button buttonNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fall);

        //선을 나타낼 수 있는 가속도 센서 선택
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(FallActivity.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        initView();
        setButton();
    }

    private void initView() {
        textViewValue = (TextView)findViewById(R.id.textViewValue);
        textViewFall = (TextView)findViewById(R.id.textViewFall);

        buttonNumber = (Button)findViewById(R.id.buttonNumber);
    }

    private void setButton() {
        buttonNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddNumberActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() != Sensor.TYPE_LINEAR_ACCELERATION) return;

        float X = event.values[0];
        float Y = event.values[1];
        float Z = event.values[2];

        float acc_x = Math.abs(x - X);
        float acc_y = Math.abs(y - Y);
        float acc_z = Math.abs(z - Z);

        double angle_x = Math.atan(Math.sqrt(acc_y * acc_y + acc_z * acc_z) / acc_x) * 180 / Math.PI;
        double angle_y = Math.atan(Math.sqrt(acc_x * acc_x + acc_z * acc_z) / acc_y) * 180 / Math.PI;
        double angle_z = Math.atan(Math.sqrt(acc_x * acc_x + acc_y * acc_y) / acc_z) * 180 / Math.PI;

        double acc_svm = Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);

        textViewValue.setText("angle_x : " + angle_x + "and agle_y : " + angle_y + "angle_z : " + angle_z + " and acc_svm : " + acc_svm);

        int cnt=0;
        if (angle_x > 60) ++cnt;
        if (angle_y > 60) ++cnt;
        if (angle_z > 60) ++cnt;

        if (acc_svm > 2.5 && cnt >= 2) {
            textViewFall.setText("angle_x : " + angle_x + "and agle_y : " + angle_y + "angle_z : " + angle_z + " and acc_svm : " + acc_svm);

            Intent intent = new Intent(getApplicationContext(), FloatActivity.class);
            startActivity(intent);
        }

        x = X; y = Y; z = Z;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}