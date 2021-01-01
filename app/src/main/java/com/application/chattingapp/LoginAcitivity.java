package com.application.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class LoginAcitivity extends AppCompatActivity {

    private EditText phoneText;
    private Button loginButton;
    private FirebaseAuth mauth=FirebaseAuth.getInstance();
    private Toolbar toolbar;
    private TextView register;
    private DatabaseReference userReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_acitivity);

        phoneText=findViewById(R.id.loginPhoneText);
        loginButton=findViewById(R.id.loginButton);
        toolbar=findViewById(R.id.loginBarLayout);
        register=findViewById(R.id.loginRegisterText);
        userReference=FirebaseDatabase.getInstance().getReference().child("Users");

        LoginAcitivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toggleIntent=new Intent(LoginAcitivity.this,RegisterActivity.class);
                startActivity(toggleIntent);

            }
        });
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login Page");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = phoneText.getText().toString();
                if (!phone.isEmpty()) {
                    Intent verifyIntent=new Intent(LoginAcitivity.this,VerifyPhoneNumber.class);
                    verifyIntent.putExtra("phoneNumber",phone);
                    startActivity(verifyIntent);

                } else {
                    Toast.makeText(LoginAcitivity.this, "Fields are empty!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}