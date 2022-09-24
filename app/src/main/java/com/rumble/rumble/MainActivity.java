package com.rumble.rumble;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.rumble.rumble.medicine.MedicineActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


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
         */
        Button buttonFall = (Button) findViewById(R.id.buttonFall);
        Button buttonSTT = (Button) findViewById(R.id.buttonSTT);
        Button buttonAlarm = (Button) findViewById(R.id.buttonDrugAlarm);

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
    }
}