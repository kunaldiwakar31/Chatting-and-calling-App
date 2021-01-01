package com.application.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class RegisterActivity extends AppCompatActivity {

    private EditText editName;
    private EditText editPhone;
    private Button createAccountButton;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private TextView login;
    private DatabaseReference database;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();
        editName=findViewById(R.id.editRegisterName);
        editPhone =findViewById(R.id.editTextPhone);
        createAccountButton=findViewById(R.id.registerAccountButton);
        progressBar=findViewById(R.id.registerProgressBar);
        login=findViewById(R.id.registerLoginText);
        toolbar=findViewById(R.id.registerPageToolbar);

        RegisterActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toggleIntent=new Intent(RegisterActivity.this,LoginAcitivity.class);
                startActivity(toggleIntent);
            }
        });
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name= editName.getText().toString();
                String phone= editPhone.getText().toString();
                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                    Toast.makeText(RegisterActivity.this,"Fields are Empty!!",Toast.LENGTH_SHORT).show();
                } else {
                    Intent verifyIntent=new Intent(RegisterActivity.this,VerifyPhoneNumber.class);
                    verifyIntent.putExtra("phoneNumber",phone);
                    verifyIntent.putExtra("name",name);
                    startActivity(verifyIntent);
//                    progressDialog=new ProgressDialog(RegisterActivity.this);
//                    progressDialog.setTitle("Registering...");
//                    progressDialog.setMessage("Please wait while we create your Account");
//                    progressDialog.setCanceledOnTouchOutside(false);
//                    progressDialog.show();
                }
            }
        });
    }
}