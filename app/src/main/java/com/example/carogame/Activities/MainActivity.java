package com.example.carogame.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.carogame.R;
import com.example.carogame.Utilities.CaroSocket;
import com.example.carogame.Utilities.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout edtName;
    private MaterialButton btnStart;
    private EditText edtURL;
    private View vMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtName = findViewById(R.id.edt_name_main);
        btnStart = findViewById(R.id.btn_start_main);
        edtURL = findViewById(R.id.edt_url);
        vMain = findViewById(R.id.v_main);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edtName.getEditText().getText().toString();

                if (edtURL.getText().toString().equals("")) {
                    Constants.SERVER_URL = Constants.DEFAULT_SERVER_URL;
                } else {
                    Constants.SERVER_URL = edtURL.getText().toString();
                }

//                CaroSocket.init();
                if (name.equals("")) {
                    Snackbar snackbar = Snackbar
                            .make(vMain, "Bạn cần điền tên để tiếp tục", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                } else {
                    Intent intent = new Intent(MainActivity.this, RoomActivity.class);
                    intent.putExtra("name", name);
                    startActivity(intent);
                }
            }
        });
    }
}