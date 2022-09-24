package com.rumble.rumble;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.rumble.rumble.medicine.MedicineActivity;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Document;
import org.w3c.dom.Element;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    String url = "https://health.chosun.com/"; // 건강정보 가져 올 웹사이트
    String healthlink;
    String healthtitle;
    TextView webtitleTextView;
    ImageView webimageImageView;
    Button buttonMovetoLink;
    String articlelink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webtitleTextView = findViewById(R.id.webTextView);
        webimageImageView = findViewById(R.id.poster);

        final Bundle bundle = new Bundle();

        Button buttonFall = (Button) findViewById(R.id.buttonFall);
        Button buttonSTT = (Button) findViewById(R.id.buttonSTT);
        Button buttonAlarm = (Button) findViewById(R.id.buttonDrugAlarm);
        buttonMovetoLink = findViewById(R.id.buttonMovetoLink);

        buttonFall.setOnClickListener(new View.OnClickListener() {
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
                    healthlink = contents1.text();
                    bundle.putString("weblink",arr[1]); // 건강기사 URL
                    bundle.putString("webimage",arr[3]); // 기사 이미지 URL
                    bundle.putString("webtitle",healthlink);
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