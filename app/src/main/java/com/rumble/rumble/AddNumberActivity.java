package com.rumble.rumble;

import androidx.annotation.Dimension;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class AddNumberActivity extends AppCompatActivity {

    static final int SMS_RECEIVE_PERMISSION = 1;

    private LinearLayout numberListLayout;
    private EditText editTextNumber;
    private ImageButton buttonAdd;
    private Switch switchService;
    private TextView switchtextview;

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_number);

        preCheck();
        initView();
        init();
        setButton();
        setLayout();
        setSwitch();
    }

    private void preCheck() {
        // 권한부여 해야함
    }

    private void initView() {
        numberListLayout = (LinearLayout) findViewById(R.id.numberListLayout);
        editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        buttonAdd = (ImageButton)findViewById(R.id.buttonAdd);
        switchService = (Switch)findViewById(R.id.switchService);
        switchtextview = (TextView) findViewById(R.id.switchtextview);
    }

    private void init() {
        dbHelper = new DBHelper(getApplicationContext(), 1);
    }

    private void setLayout() {
        numberListLayout.removeAllViews();
        TextView textViewTitle = new TextView(this);
        textViewTitle.setText("번호 리스트");
        textViewTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        //textViewTitle.setBackgroundColor(Color.parseColor("#000000"));
        numberListLayout.addView(textViewTitle);

        List<String> list = dbHelper.getResult();

        for(String phone : list) {
            TextView textView = new TextView(this);
            textView.setText(phone);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            // 번호 리스트 스타일 설정
            textView.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.layout_addnum_list_bg));
            textView.setTextSize(Dimension.SP, 30);
            //textView.setGravity(Gravity.CENTER);
            textView.setPadding(50,10,10,10);
            int SIZE = 15; // 원하는 사이즈
            float scale = textView.getResources().getDisplayMetrics().density;
            int startDP = (int) (SIZE * scale);
            int topDP = (int) (SIZE * scale);
            int rightDP = (int) (SIZE * scale);
            int bottomDP = (int) (SIZE * scale);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(startDP, topDP, rightDP, bottomDP);
            textView.setLayoutParams(layoutParams);

            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddNumberActivity.this);

                    builder.setMessage("전화번호 삭제")
                            .setTitle(textView.getText().toString() + " 전화번호를 정말 삭제하시겠습니까?");

                    builder.setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dbHelper.Delete(textView.getText().toString());
                            setLayout();
                            Toast.makeText(getApplicationContext(), "번호가 삭제되었습니다", Toast.LENGTH_LONG).show();
                        }
                    });

                    builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });

                    builder.show();
                    return true;
                }
            });

            numberListLayout.addView(textView);
        }
    }

    private void setButton() {
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = editTextNumber.getText().toString();
                if(dbHelper.insert(phoneNumber)) {
                    Toast.makeText(getApplicationContext(), "성공적으로 번호를 추가했습니다.", Toast.LENGTH_LONG).show();
                    setLayout();
                } else {
                    Toast.makeText(getApplicationContext(), "번호를 다시 확인해주세요.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setSwitch() {
        ActivityManager am = (ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(1000);

        boolean flag = false;

        for(int i=0;i<rs.size();i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            Log.d("FallService", "package : " + rsi.service.getPackageName());
            Log.d("FallService", "class : " + rsi.service.getClassName());

            if (rsi.service.getClassName().equals("com.rumble.rumble.FallService")) {
                flag = true;
                break;
            }
        }

        Log.d("FallService", flag + "");

        if (flag) {
            switchService.setChecked(true);
            switchService.setText("켜짐");
            switchtextview.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.layout_switch_bg));
            switchService.setTextColor(Color.parseColor("#ffffff"));
        }
        else {
            switchService.setChecked(false);
            switchService.setText("꺼짐");
            switchtextview.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.layout_switch_bgoff));
            switchService.setTextColor(Color.parseColor("#000000"));
        }

        switchService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(switchService.isChecked()) {
                    switchService.setText("켜짐");
                    switchtextview.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.layout_switch_bg));
                    switchService.setTextColor(Color.parseColor("#ffffff"));

                    // 넘어짐감지 백그라운드 쓰레드 유지
                    Intent intentse = new Intent(getApplicationContext(),FallService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intentse);
                    }
                    else{
                        startActivity(intentse);
                    }
                }
                else {
                    switchService.setText("꺼짐");
                    switchtextview.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.layout_switch_bgoff));
                    switchService.setTextColor(Color.parseColor("#000000"));

                    /*
                    try {
                        ((FallService)FallService.mContext).onStopService();
                    } catch (Exception e) {
                        Log.d("FallService", "" + ((FallService)FallService.mContext));
                    }
                    */

                    Intent intentse = new Intent(getApplicationContext(),FallService.class);
                    stopService(intentse);
                }
            }
        });
    }
}