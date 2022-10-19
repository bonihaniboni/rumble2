package com.rumble.rumble;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class PopUpActivity extends AppCompatActivity {

    private Button buttonYes, buttonNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pop_up);

        initView();
    }

    private void initView() {
        buttonYes = (Button)findViewById(R.id.buttonYes);
        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddNumberActivity.class);
                startActivity(intent);

                finish();
            }
        });

        buttonNo = (Button)findViewById(R.id.buttonNo);
        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}