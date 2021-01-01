package com.application.chattingapp;

import android.app.Application;
import android.app.Service;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class ConversationApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final DatabaseReference userDatabase;
        FirebaseAuth user;
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        //picasso offline image storage

        Picasso.Builder builder=new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this,Integer.MAX_VALUE));
        Picasso built=builder.build();
        builder.indicatorsEnabled(true);
        builder.loggingEnabled(true);
        Picasso.setSingletonInstance(built);

        user=FirebaseAuth.getInstance();
        if(user.getCurrentUser()!=null) {
            userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getCurrentUser().getUid());

            userDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    if (snapshot != null) {
                        userDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
}
