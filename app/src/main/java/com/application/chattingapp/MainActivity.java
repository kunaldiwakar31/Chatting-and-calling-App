package com.application.chattingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar toolbar;
    private ViewPager viewPager;
    private SectionPagerAdapter sectionPagerAdapter;
    private TabLayout tabLayout;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        toolbar=(Toolbar) findViewById(R.id.mainPageToolbar);
        setSupportActionBar(toolbar);
        rootRef= FirebaseDatabase.getInstance().getReference();
        getSupportActionBar().setTitle("Conversation App");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_launcher_round);

        viewPager=findViewById(R.id.mainPageTaber);
        sectionPagerAdapter=new SectionPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionPagerAdapter);
        tabLayout=findViewById(R.id.mainTabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void sendToStart(){
        Intent intent=new Intent(MainActivity.this,StartActivity.class);
        startActivity(intent);
        finish();
    }
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null) {
            sendToStart();
        }else{
            rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue("True");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.mainmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.mainLogout){
            rootRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        if(item.getItemId()==R.id.mainAccountSetting){
            Intent settingsIntent=new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if(item.getItemId()==R.id.mainAllUsers){
            Intent userIntent=new Intent(MainActivity.this,UsersActivity.class);
            startActivity(userIntent);
        }
        return true;
    }
}