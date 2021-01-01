package com.application.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileName;
    private TextView profileStatus;
    private Button sendRequest;
    private ImageView profileImage;
    private DatabaseReference profileReference;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private DatabaseReference requestReference;
    private DatabaseReference friendReference;
    private DatabaseReference notificationReference;
    private FirebaseUser currentUser;
    private String currentStats;
    private Button declineRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileName=findViewById(R.id.profileName);
        profileStatus=findViewById(R.id.profileStatus);
        profileImage=findViewById(R.id.profileImage);
        sendRequest=findViewById(R.id.profileSenRequest);
        declineRequest=findViewById(R.id.profileDeclineRequest);

        currentStats="notFriends";
        final String userID=getIntent().getStringExtra("userId");
        final String userName=getIntent().getStringExtra("name");

        progressDialog=new ProgressDialog(ProfileActivity.this);
        progressDialog.setTitle("Registering...");
        progressDialog.setMessage("Please wait while we create your Account");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        requestReference=FirebaseDatabase.getInstance().getReference().child("friendRequest");
        friendReference=FirebaseDatabase.getInstance().getReference().child("friends");
        notificationReference=FirebaseDatabase.getInstance().getReference().child("notification");
        profileReference= FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        declineRequest.setEnabled(false);

        profileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profile_Name=snapshot.child("name").getValue().toString();
                String profile_Image=snapshot.child("image").getValue().toString();
                String profile_Status=snapshot.child("status").getValue().toString();

                profileName.setText(profile_Name);
                profileStatus.setText(profile_Status);
                Picasso.get().load(profile_Image).placeholder(R.drawable.profilepic3).into(profileImage);
                progressDialog.dismiss();

                requestReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChild(userID)){
                            String reqtype=snapshot.child(userID).child("requestType").getValue().toString();
                            if(reqtype.equals("friendSent")){
                                currentStats="friendSent";
                                sendRequest.setText("Cancel Request");
                                declineRequest.setVisibility(View.INVISIBLE);
                                declineRequest.setEnabled(false);
                            }else if(reqtype.equals("friendRecieved")){
                                currentStats="friendRecieved";
                                sendRequest.setText("Accept Friend Request");
                                declineRequest.setVisibility(View.VISIBLE);
                                declineRequest.setEnabled(true);
                            }
                        }else {
                            friendReference.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.hasChild(userID)){
                                        currentStats="friends";
                                        sendRequest.setText("Unfriend "+userName);
                                        declineRequest.setVisibility(View.INVISIBLE);
                                        declineRequest.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest.setEnabled(false);

/*-----------------when users are not friends/we are sending friend request---------------*/

                if(currentStats.equals("notFriends")) {
                    requestReference.child(currentUser.getUid()).child(userID).child("requestType")
                            .setValue("friendSent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                requestReference.child(userID).child(currentUser.getUid()).child("requestType")
                                        .setValue("friendRecieved").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String,String> notificationData=new HashMap<>();
                                        notificationData.put("from",currentUser.getUid());
                                        notificationData.put("type","request");
                                        notificationReference.child(userID).push().setValue(notificationData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    sendRequest.setEnabled(true);
                                                    currentStats = "friendSent";
                                                    sendRequest.setText("Cancel Request");
                                                }else{
                                                    Toast.makeText(ProfileActivity.this, "Failed to sent request!! Please try again.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }
                                });
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed to sent request!! Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                /*-----------------when current user have sent friend request---------------*/

                if(currentStats.equals("friendSent")){
                    requestReference.child(currentUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                requestReference.child(userID).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){

                                            currentStats="notFriends";
                                            sendRequest.setEnabled(true);
                                            sendRequest.setText("send friend request");

                                        }else{
                                            Toast.makeText(ProfileActivity.this,"Failed to cancel request!! Please try again.",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                Toast.makeText(ProfileActivity.this,"Failed to cancel request!! Please try again.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                /*-----------------when current user have recieved friend request/we accept the friend request---------------*/

                if(currentStats.equals("friendRecieved")) {

                    final String currentDate= DateFormat.getDateTimeInstance().format(new Date());
                    friendReference.child(currentUser.getUid()).child(userID).child("Date").setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                friendReference.child(userID).child(currentUser.getUid()).child("Date").setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            requestReference.child(currentUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()) {
                                                        requestReference.child(userID).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){

                                                                    currentStats="friends";
                                                                    sendRequest.setEnabled(true);
                                                                    sendRequest.setText("Unfriend "+userName);
                                                                    declineRequest.setVisibility(View.INVISIBLE);
                                                                    declineRequest.setEnabled(false);

                                                                }else{
                                                                    Toast.makeText(ProfileActivity.this,"Failed to Accept request!! Please try again.",Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }else{
                                                        Toast.makeText(ProfileActivity.this,"Failed to Accept request!! Please try again.",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }else{
                                            Toast.makeText(ProfileActivity.this,"Failed to Accept request!! Please try again.",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                Toast.makeText(ProfileActivity.this,"Failed to Accept request!! Please try again.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                /*-----------------when users are friends/user presses unfriend button---------------*/

                if(currentStats.equals("friends")){

                    friendReference.child(currentUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                friendReference.child(currentUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(ProfileActivity.this,"Unfriended "+userName,Toast.LENGTH_SHORT).show();
                                            sendRequest.setText("Send Friend Request");
                                            currentStats="notFriends";
                                        }else{
                                            Toast.makeText(ProfileActivity.this,"Failed to unfriend the user!! Please try again.",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else{
                                Toast.makeText(ProfileActivity.this,"Failed to unfriend the user!! Please try again.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        declineRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestReference.child(userID).child(currentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            requestReference.child(currentUser.getUid()).child(userID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        currentStats="notFriends";
                                        sendRequest.setText("Send friend request");
                                        declineRequest.setVisibility(View.INVISIBLE);
                                        declineRequest.setEnabled(false);
                                    }else{
                                        Toast.makeText(ProfileActivity.this,"Failed to decline request user!! Please try again.",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else {
                            Toast.makeText(ProfileActivity.this,"Failed to decline request user!! Please try again.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}