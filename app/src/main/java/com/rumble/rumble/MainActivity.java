package com.rumble.rumble;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    String url = "https://health.chosun.com/"; // 건강정보 가져 올 웹사이트
    String healthlink; // 크롤링
    TextView webtitleTextView;
    ImageView webimageImageView, buttonAlarm, buttonSTT;
    String articlelink;

    // 만보기 관련 변수
    SharedPreferences preferences; //
    SharedPreferences.Editor editor;
    String savedate;
    String nowdate;
    private TextView textViewWalk; // 현재 걸음 수 표시
    private TextView datetextview;
    public static String format_yyyyMMdd = "yyyyMMdd";

    SharedPreferences preferencesWalk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent itit = new Intent(this,PedometerService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(itit);
        } else {
            startService(itit);
        }

        // 원 UI 스타일 앱바로 만들어주는 코드
        CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout)
                findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);

        // 만보기 텍스트뷰
        textViewWalk = (TextView)findViewById(R.id.textViewWalk);
        datetextview = (TextView)findViewById(R.id.datedate);
        preferencesWalk = getSharedPreferences("Walk", MODE_PRIVATE);
        preCheck();

        preferences = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = preferences.edit();
        savedate = preferences.getString("Date","-"); // 저장해 둔 값 불러오기 없으면 -
        nowdate = getCurrentDate_yyyyMMdd();
        if(savedate != nowdate){ // 날짜 바뀌면

        }
        else{

        }
        datetextview.setText(savedate);


        webtitleTextView = findViewById(R.id.webTextView);
        webimageImageView = findViewById(R.id.poster);

        final Bundle bundle = new Bundle();

        FloatingActionButton buttonfallsetting = findViewById(R.id.buttonFallsetting);
        buttonSTT = findViewById(R.id.buttonSTT);
        buttonAlarm = findViewById(R.id.buttonDrugAlarm);

        buttonfallsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddNumberActivity.class);
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

        webimageImageView.setOnClickListener(new View.OnClickListener() {
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

        // 건강정보 크롤링 관련
        // ------------------------------------------------------------------------------------------------------------
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

    // ------------------------------------------------------------------------------------------------------------
    // 건강정보 크롤링 관련

    // 만보기 관련
    // ------------------------------------------------------------------------------------------------------------
    // 현재 날짜 구하는 메서드
    public static String getCurrentDate_yyyyMMdd() {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat(format_yyyyMMdd, Locale.getDefault());
        return format.format(currentTime);
    }

    // 인텐트로 값 수신
    private void processIntent(Intent intent) {
        if (intent != null) {
            String command = intent.getStringExtra("command");
            String name = intent.getStringExtra("name");
            Log.d("test","수신"); // 여기까진 들어가진다 즉 인텐트가 null값이 아니다
            //Log.d("test",command); // 하지만 열어보면 null
            //Toast.makeText(this, "command : " + command + ", name : " + name, Toast.LENGTH_LONG).show();
            if(command != null) textViewWalk.setText(command);
            Log.d("WalkingChecker", "proceddIntent");
            Log.d("WalkingChecker", "after command : " + command);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);

        super.onNewIntent(intent);
    }

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

    @Override
    public void onResume() {
        super.onResume();
        //Log.d("WalkingChecker", "onResume");
        Log.d("WalkingChecker", textViewWalk.getText().toString());
        textViewWalk.invalidate();
    }
}