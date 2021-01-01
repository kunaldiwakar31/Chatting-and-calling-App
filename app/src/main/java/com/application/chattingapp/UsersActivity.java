package com.application.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private EditText searchUsers;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        toolbar=findViewById(R.id.userLayout);
        searchUsers=findViewById(R.id.userSearch);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("All Users");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.back_icon);

        recyclerView=findViewById(R.id.userRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.keepSynced(true);
        searchUsers.setVisibility(View.GONE);

        currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.users_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.userSearchMenu){
            makeSearch();
        }else if(item.getItemId()==android.R.id.home){
            finish();
        }
        return true;
    }

    private void makeSearch() {
        searchUsers.setVisibility(View.VISIBLE);
        searchUsers.requestFocus();
        searchUsers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUser(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(databaseReference, Users.class)
                        .build();

        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter=getFirebaseRecyclerAdapter(options);

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public FirebaseRecyclerAdapter<Users, UsersViewHolder> getFirebaseRecyclerAdapter(FirebaseRecyclerOptions<Users> options) {

        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.singleuserlayout,parent, false);
                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder usersViewHolder, int i, @NonNull Users users) {
                usersViewHolder.setName(users.getName());
                usersViewHolder.setStatus(users.getStatus());
                usersViewHolder.setUserImage(users.getThumb_image(),getApplicationContext());

                final String userId=getRef(i).getKey();
                final String name=users.getName();
                usersViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        assert userId != null;
                        if(!userId.equals(currentUserId)) {
                            Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("userId", userId);
                            profileIntent.putExtra("name", name);
                            startActivity(profileIntent);
                        }else{
                            Intent profileIntent = new Intent(UsersActivity.this, SettingsActivity.class);
                            startActivity(profileIntent);
                        }
                    }
                });
            }
        };
        return firebaseRecyclerAdapter;
    }

    private void searchUser(final String user) {
        final String currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        final ArrayList<Users> mUsers=new ArrayList<>();
        final Query query=FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("search")
                .startAt(user).endAt(user+"\uf0ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Users users=snapshot1.getValue(Users.class);

                    if(!snapshot1.getKey().equals(currentUserId))
                        mUsers.add(users);
                }
                FirebaseRecyclerOptions<Users> options =
                        new FirebaseRecyclerOptions.Builder<Users>()
                                .setQuery(query, Users.class)
                                .build();
                FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter=getFirebaseRecyclerAdapter(options);
                recyclerView.setAdapter(firebaseRecyclerAdapter);
                firebaseRecyclerAdapter.startListening();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View view;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            view=itemView;
        }
        public void setName(String name){
            TextView username=view.findViewById(R.id.singleUserName);
            username.setText(name);
        }
        public void setStatus(String status){
            TextView userStatus=view.findViewById(R.id.singleUserStatus);
            userStatus.setText(status);
        }
        public void setUserImage(String thumb_image, Context applicationContext){
            CircleImageView userPic=view.findViewById(R.id.singleUserImage);
            Picasso.get().load(thumb_image).placeholder(R.drawable.profilepic).into(userPic);
        }
    }

}