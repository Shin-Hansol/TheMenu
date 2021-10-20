package com.example.getmenu;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class ResultActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        textView = findViewById(R.id.tvResult);
        textView.setMovementMethod(new ScrollingMovementMethod());

        Intent intent = getIntent();
        String result = intent.getExtras().getString("result");
        textView.setText(result);
    }
}