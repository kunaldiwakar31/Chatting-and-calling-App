package com.application.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.TaskExecutor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VerifyPhoneNumber extends AppCompatActivity {

    private Button verifyButton;
    private EditText otpText;
    private ProgressBar progressBar;
    private String verificationCodeByUser;
    private DatabaseReference database;
    private String name;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_phone_number);

        verifyButton=findViewById(R.id.verifyButton);
        otpText=findViewById(R.id.verifyOtp);
        progressBar=findViewById(R.id.verifyProgressBar);

        phoneNumber=getIntent().getStringExtra("phoneNumber");
        name=getIntent().getStringExtra("name");
        sendVerificationCodeToUser(phoneNumber);

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String code=otpText.getText().toString();
                if(!code.isEmpty() && !(code.length() <6)){
                    progressBar.setVisibility(View.VISIBLE);
                    verifyCode(code);
                }else{
                    otpText.setError("Incorrect OTP!");
                    otpText.requestFocus();
                }
            }
        });
    }

    private void sendVerificationCodeToUser(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91"+phoneNumber,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks);
    }
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationCodeByUser=s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            String code=phoneAuthCredential.getSmsCode();
            if(code!=null){
                progressBar.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(VerifyPhoneNumber.this,e.getMessage(),Toast.LENGTH_LONG).show();
        }
    };
    private void verifyCode(String codeByUser) {
        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationCodeByUser,codeByUser);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressBar.setVisibility(View.INVISIBLE);

                    if(name==null){
                        Intent mainIntent=new Intent(VerifyPhoneNumber.this,MainActivity.class);
                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);
                        finish();
                    }else {
                        FirebaseUser currentUser=FirebaseAuth.getInstance().getCurrentUser();
                        String uid=currentUser.getUid();
                        database= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                        HashMap<String, String> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("status", "Hey there, I'm using Chatting App");
                        userMap.put("online", "True");
                        userMap.put("phoneNumber", phoneNumber);
                        userMap.put("image", "default");
                        userMap.put("thumb_image", "default");
                        userMap.put("search", name.toLowerCase());

                        database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent mainIntent = new Intent(VerifyPhoneNumber.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        });
                    }

                }else{
                    Toast.makeText(VerifyPhoneNumber.this, Objects.requireNonNull(task.getException()).getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}