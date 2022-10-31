package com.rumble.rumble;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.rumble.rumble.medicine.MedicineActivity;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.security.auth.callback.PasswordCallback;

public class MainActivity extends AppCompatActivity {

    final int PERMISSION = 1; // 권한 변수

    String url = "https://health.chosun.com/"; // 건강정보 가져 올 웹사이트
    String healthlink; // 크롤링
    TextView webtitleTextView;
    ImageView webimageImageView, buttonAlarm, buttonSTT, buttonhelp;
    String articlelink;
    private CustomDialog customDialog;

    // 만보기 관련 변수
    SharedPreferences preferences; //
    SharedPreferences.Editor editor;
    String savedate;
    String nowdate;
    private TextView textViewWalk; // 현재 걸음 수 표시
    private TextView datetextview;
    public static String format_yyyyMMdd = "yyyyMMdd";

    SharedPreferences preferencesWalk;

    private PermissionSupport permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstCheck();

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
        //textViewWalk = (TextView)findViewById(R.id.textViewWalk);
        //datetextview = (TextView)findViewById(R.id.datedate);
        preferencesWalk = getSharedPreferences("Walk", MODE_PRIVATE);
        //preCheck();

        preferences = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = preferences.edit();
        savedate = preferences.getString("Date","-"); // 저장해 둔 값 불러오기 없으면 -
        nowdate = getCurrentDate_yyyyMMdd();
        if(savedate != nowdate){ // 날짜 바뀌면

        }
        else{

        }
        //datetextview.setText(savedate);


        webtitleTextView = findViewById(R.id.webTextView);
        webimageImageView = findViewById(R.id.poster);

        final Bundle bundle = new Bundle();

        FloatingActionButton buttonfallsetting = findViewById(R.id.buttonFallsetting);
        buttonSTT = findViewById(R.id.buttonSTT);
        buttonAlarm = findViewById(R.id.buttonDrugAlarm);
        buttonhelp = findViewById(R.id.buttonhelp);


        buttonfallsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddNumberActivity.class);
                startActivity(intent);
            }
        });
        buttonhelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customDialog = new CustomDialog(MainActivity.this);
                customDialog.show();
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

    private void setAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("권한 설정")
                .setMessage("권한 거절 시 앱을 실행할 수 없습니다.")
                .setPositiveButton("권한 설정하러 가기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                                    .setData(Uri.parse("package:"+getPackageName()));
                            startActivity(intent);
                        } catch(Exception e) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                            startActivity(intent);
                        }
                    }
                })
                .setNegativeButton("취소하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((MainActivity)getApplicationContext()).finish();
                    }
                })
                .create()
                .show();
    }

    // 만보기 권한 체크
    private void preCheck() {
        if (Build.VERSION.SDK_INT >= 23) {
            // 권한 없을 시 권한 요청

            PermissionListener permissionListener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {

                }

                @Override
                public void onPermissionDenied(List<String> deniedPermissions) {

                }
            };

            TedPermission.with(this)
                    .setPermissionListener(permissionListener)
                    .setRationaleMessage("앱 기능을 위해 일부 권한이 필요합니다.")
                    .setDeniedMessage("권한 거부시 앱 실행이 안됩니다.")
                    .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACTIVITY_RECOGNITION,
                            Manifest.permission.SEND_SMS)
                    .check();


            /*
            permission = new PermissionSupport(this, this);
            permission.requestPermission();

            if(!permission.checkPermission()) {
                permission.requestPermission();
            }

             */

        }
    }

    private void firstCheck() {
        SharedPreferences pref = getSharedPreferences("isFirst", Activity.MODE_PRIVATE);
        boolean first = pref.getBoolean("isFirst", false);
        //Log.d("FirstCheck", "firstCheck()");

        if(first==false) {
            //Log.d("FirstCheck", "first");
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("isFirst", true);
            editor.commit();

            Intent intent = new Intent(this, PopUpActivity.class);
            startActivityForResult(intent, 1);
        } else {
            //Log.d("FirstCheck", "not first");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        preCheck();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}