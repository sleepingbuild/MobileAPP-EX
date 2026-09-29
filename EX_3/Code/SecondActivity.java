package com.example.ex1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView svName, svMSSV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        btnBack = findViewById(R.id.btnBack);
        svName = findViewById(R.id.svName);
        svMSSV = findViewById(R.id.svMSSV);


        Intent intent = getIntent();
        if (intent != null) {
            String name = intent.getStringExtra("EXTRA_NAME");
            String mssv = intent.getStringExtra("EXTRA_MSSV");

            svName.setText("Name: " + name);
            svMSSV.setText("Student ID: " + mssv);
        }


        btnBack.setOnClickListener(v -> finish());
    }
}
