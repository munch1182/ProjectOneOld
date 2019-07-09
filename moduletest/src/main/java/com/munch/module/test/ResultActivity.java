package com.munch.module.test;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Munch on 2019/7/9 11:21
 */
public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView child = new TextView(this);
        child.setText("RESULT OK");
        child.setOnClickListener(v -> {
            Intent data = new Intent();
            data.putExtra("1233",1);
            setResult(RESULT_OK, data);
            finish();
        });
        layout.addView(child);

        TextView child2 = new TextView(this);
        child2.setText("RESULT CANCELED");
        child2.setOnClickListener(v -> {
            setResult(RESULT_CANCELED,null);
            finish();
        });
        layout.addView(child2);

        TextView child3 = new TextView(this);
        child3.setText("RESULT 3");
        child3.setOnClickListener(v -> {
            setResult(55555,null);
            finish();
        });
        layout.addView(child3);
        setContentView(layout);
    }
}
