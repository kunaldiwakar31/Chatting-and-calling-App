package com.application.chattingapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button registerButton;
    private Button loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        registerButton=findViewById(R.id.startRegisterButton);
        loginButton=findViewById(R.id.startLoginButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent regIntent=new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(regIntent);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent=new Intent(StartActivity.this,LoginAcitivity.class);
                startActivity(loginIntent);
            }
        });
    }
}