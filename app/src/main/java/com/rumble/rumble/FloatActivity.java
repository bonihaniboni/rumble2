package com.rumble.rumble;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class FloatActivity extends AppCompatActivity {

    private CountDownTimer countDownTimer;
    private TextView textViewTime;
    private Button buttonOK, buttonAlert;

    private DBHelper dbHelper;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float);

        preCheck();
        initView();
        init();
        initLocation();
        setButton();
    }

    private void preCheck() {
        // 권한 체크 해줘야함
    }

    private void initLocation() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "위치 정보 권한 허용해주세요", Toast.LENGTH_LONG).show();
            return;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            Toast.makeText(getApplicationContext(), "위치를 불러오는데 실패했습니다", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Log.d("getGPSA", "" + location.getLatitude());
                        Log.d("getGPSA", "" + location.getLongitude());

                        currentLocation = location;
                    }
                });
    }

    private void sendMessage() {
        List<String> list = dbHelper.getResult();
        int count = 0;

        for(String phoneNumber : list) {
            ++count;

            String sms = "위험 감지 (테스트용)\n\n";
            if(currentLocation != null) {
                sms += "http://maps.google.com/?q=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude();
            }
            else {
                sms += "위치정보를 파악할 수 없습니다";
            }
            Log.d("LogNumber", phoneNumber);

            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, sms, null, null);

            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "failed to send sms", Toast.LENGTH_LONG).show();
            }
        }

        if (count == 0) {
            Toast.makeText(getApplicationContext(), "저장된 번호가 없어요.", Toast.LENGTH_LONG).show();
        }
    }

    private void init() {
        dbHelper = new DBHelper(getApplicationContext(), 1);

        countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
                textViewTime.setText(l / 1000 + "초 후 자동 신고");
            }

            @Override
            public void onFinish() {
                sendMessage();
                finish();
            }
        }.start();
    }

    private void initView() {
        textViewTime = (TextView)findViewById(R.id.textViewTime);

        buttonOK = (Button)findViewById(R.id.buttonOK);
        buttonAlert = (Button)findViewById(R.id.buttonAlert);
    }

    private void setButton() {
        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDownTimer.cancel();
                finish();
            }
        });

        buttonAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                finish();
            }
        });
    }
}