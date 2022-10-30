package com.rumble.rumble;

import androidx.annotation.NonNull;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

public class CustomDialog extends Dialog {
    private Context mContext;
    private TextView txt_contents;
    private Button shutdownClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_dialog);

        // 다이얼로그의 배경을 투명으로 만든다.
        //Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        txt_contents = findViewById(R.id.txt_contents);
        shutdownClick = findViewById(R.id.btn_shutdown);

        shutdownClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }
    public CustomDialog(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }
}