package com.example.ex1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText edtUserName, editMSSV;
    private Button btnClickMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtUserName = findViewById(R.id.edtUserName);
        editMSSV = findViewById(R.id.editMSSV);
        btnClickMe = findViewById(R.id.btnClickMe);

        btnClickMe.setOnClickListener(v -> {
            String name = edtUserName.getText().toString().trim();
            String mssv = editMSSV.getText().toString().trim();

            String mssvPattern = "^B[A-Z]{2}2[2-6][0-9]{4}$";

            if (name.isEmpty() && mssv.isEmpty()) {
                showWarningDialog("Vui lòng nhập cả Họ tên và MSSV!");
                edtUserName.requestFocus();
            } else if (name.isEmpty()) {
                showWarningDialog("Vui lòng nhập Họ và tên!");
                edtUserName.requestFocus();
            } else if (mssv.isEmpty()) {
                showWarningDialog("Vui lòng nhập Mã số sinh viên (MSSV)!");
                editMSSV.requestFocus();
            } else if (!mssv.matches(mssvPattern)) {
                showWarningDialog("MSSV không đúng định dạng!\n\nQuy tắc: B + 2 chữ hoa + 22..26 + 4 số\nVí dụ đúng: BIT240177");
                editMSSV.requestFocus();
            } else {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                intent.putExtra("EXTRA_NAME", name);
                intent.putExtra("EXTRA_MSSV", mssv);
                startActivity(intent);
            }
        });
    }

    private void showWarningDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Cảnh báo")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(true)
                .show();
    }
}
