package com.rumble.rumble;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class AddNumberActivity extends AppCompatActivity {

    static final int SMS_RECEIVE_PERMISSION = 1;

    private LinearLayout numberListLayout;
    private EditText editTextNumber;
    private Button buttonAdd;

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
    }

    private void preCheck() {
        // 권한부여 해야함
    }

    private void initView() {
        numberListLayout = (LinearLayout) findViewById(R.id.numberListLayout);
        editTextNumber = (EditText) findViewById(R.id.editTextNumber);
        buttonAdd = (Button)findViewById(R.id.buttonAdd);
    }

    private void init() {
        dbHelper = new DBHelper(getApplicationContext(), 1);
    }

    private void setLayout() {
        numberListLayout.removeAllViews();
        List<String> list = dbHelper.getResult();

        for(String phone : list) {
            TextView textView = new TextView(this);
            textView.setText(phone);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

            numberListLayout.addView(textView);
        }
    }

    private void setButton() {
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = editTextNumber.getText().toString();
                dbHelper.insert(phoneNumber);
                setLayout();

//                String sms = "위험 감지 (테스트용)";
//                Log.d("LogNumber", phoneNumber);
//
//                try {
//                    SmsManager smsManager = SmsManager.getDefault();
//                    smsManager.sendTextMessage(phoneNumber, null, sms, null, null);
//
//                } catch (Exception e) {
//                    Toast.makeText(getApplicationContext(), "failed to send sms", Toast.LENGTH_LONG).show();
//                }
            }
        });
    }
}