package com.rumble.rumble;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rumble.rumble.medicine.MedicineActivity;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;
import org.w3c.dom.Element;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    String url = "https://health.chosun.com/"; // 건강정보 가져 올 웹사이트
    String healthlink; // 크롤링
    TextView webtitleTextView;
    ImageView webimageImageView;
    Button buttonMovetoLink;
    String articlelink;

    // 만보기 관련 변수
    private TextView textViewWalk;
    private int countWalk = 0;
    private SensorManager sensorManager;
    private Sensor stepCountSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 넘어짐감지 백그라운드 쓰레드 유지
        Intent intentse = new Intent(this,FallService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intentse);
        }
        else{
            startActivity(intentse);
        }

        // 원 UI 스타일 앱바로 만들어주는 코드
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)
                findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);

        // 만보기 텍스트뷰
        textViewWalk = (TextView)findViewById(R.id.textViewWalk);
        textViewWalk.setText("현재 걸음 수 : " + countWalk);
        preCheck(); // 만보기 권한 체크
        setSensor(); // 만보기 센서 설정

        webtitleTextView = findViewById(R.id.webTextView);
        webimageImageView = findViewById(R.id.poster);

        final Bundle bundle = new Bundle();

        FloatingActionButton buttonfallsetting = findViewById(R.id.buttonFallsetting);
        Button buttonSTT = (Button) findViewById(R.id.buttonSTT);
        Button buttonAlarm = (Button) findViewById(R.id.buttonDrugAlarm);
        buttonMovetoLink = findViewById(R.id.buttonMovetoLink);

        buttonfallsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FallActivity.class);
                startActivity(intent);
            }
        });
        buttonSTT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), STTActivity.class);
                startActivity(intent);
            }
        });
        buttonAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MedicineActivity.class);
                startActivity(intent);
            }
        });
        buttonMovetoLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(articlelink != null) {
                    Intent urlintent = new Intent(Intent.ACTION_VIEW, Uri.parse(articlelink));
                    startActivity(urlintent);
                }
                else{
                    Toast.makeText(getApplicationContext(),"잠시만 기다려주세요",Toast.LENGTH_SHORT).show();
                }
            }
        });

        new Thread(){
            @Override
            public void run(){
                Document doc = null;
                try{
                    // 크롤링 할 구문
                    doc = Jsoup.connect(url).get();
                    Elements contents = doc.select("div.top_news dl dd a"); // top.news 클래스 밑에 dl태그 밑 dd태그 밑 a로
                    healthlink = contents.toString();
                    String[] arr = healthlink.split("\"",100); // 큰따옴표 기준으로 split
                    Elements contents1 = doc.select("div.top_news h2 a"); // top.news 클래스 밑에 h2태그 밑 a로
                    healthlink = contents1.text(); // 기사 제목
                    bundle.putString("weblink",arr[1]); // 건강기사 URL
                    bundle.putString("webimage",arr[3]); // 기사 이미지 URL
                    bundle.putString("webtitle",healthlink); // 기사 제목 String
                    Message msg = handler.obtainMessage();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

        // 핸들러
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                Bundle bundle = msg.getData();
                webtitleTextView.setText(bundle.getString("webtitle"));
                articlelink = bundle.getString("weblink");
                Glide.with(getApplicationContext()).load(bundle.getString("webimage")).into(webimageImageView);
            }
        };

    // 만보기 권한 체크
    private void preCheck() {
        // 권한 체크 뭔가 잘 안됨 -> 수정 필요
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
            }
        }
    }

    // 만보기 sensor 설정
    private void setSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        if (stepCountSensor == null) {
            Toast.makeText(this, "No step Sensor", Toast.LENGTH_LONG).show();
        }

        sensorManager.registerListener(this, stepCountSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    // 센서 변화 감지
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            ++countWalk;
            textViewWalk.setText("현재 걸음 수 : " + countWalk);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    // 배터리 절전모드 강제 해제
        /*
        Intent i = new Intent();

        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                i.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            } else{
                i.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                i.setData(Uri.parse("package:" + packageName));
            }
            startActivity(i);
        }
*/

}