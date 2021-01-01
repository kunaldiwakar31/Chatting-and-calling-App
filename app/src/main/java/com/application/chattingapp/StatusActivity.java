package com.application.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout textInputLayout;
    private EditText changeStatusText;
    private Button changeStatus;
    private
    DatabaseReference databaseReference;
    FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        String currentUid=currentUser.getUid();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);

        textInputLayout=findViewById(R.id.textInputLayout);
        changeStatus=findViewById(R.id.statusChangeButton);
        changeStatusText=findViewById(R.id.statusChangeText);
        toolbar=findViewById(R.id.statusAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Change Your Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent=getIntent();
        String currentStatus= intent.getStringExtra("status");
        changeStatusText.setText(currentStatus);

        changeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String UpdatedStatus = changeStatusText.getText().toString();
                databaseReference.child("status").setValue(UpdatedStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(StatusActivity.this,"Status Updated Successfully",Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }
}