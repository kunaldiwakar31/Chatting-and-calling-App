package com.application.chattingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


import de.hdodenhof.circleimageview.CircleImageView;


public class FriendFragment extends Fragment {

    private RecyclerView friendRecyclerView;
    private DatabaseReference friendReference;
    private DatabaseReference userReference;
    private String currentUserId;
    private View mView;
    private FirebaseAuth friendAuth;

    public FriendFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView=inflater.inflate(R.layout.fragment_friend, container, false);

        friendRecyclerView=mView.findViewById(R.id.friendRecyclerView);
        friendAuth=FirebaseAuth.getInstance();
        currentUserId=friendAuth.getCurrentUser().getUid();
        friendReference= FirebaseDatabase.getInstance().getReference().child("friends");
        friendReference.keepSynced(true);
        userReference=FirebaseDatabase.getInstance().getReference().child("Users");
        userReference.keepSynced(true);

        friendRecyclerView.setHasFixedSize(true);
        friendRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<friends> options =
                new FirebaseRecyclerOptions.Builder<friends>()
                        .setQuery(friendReference.child(currentUserId), friends.class)
                        .build();

        FirebaseRecyclerAdapter<friends,FriendViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<friends, FriendViewHolder>(options) {

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.singleuserlayout,parent, false);
                return new FriendViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendViewHolder friendViewHolder, int i, @NonNull friends friends) {
                final String userId=getRef(i).getKey();

                userReference.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull final DataSnapshot snapshot) {
                        final String userName=snapshot.child("name").getValue().toString();
                        String userStatus=snapshot.child("status").getValue().toString();
                        String userThumb=snapshot.child("thumb_image").getValue().toString();
                        if(snapshot.hasChild("online")) {
                            String onlineStatus =snapshot.child("online").getValue().toString();
                            friendViewHolder.setOnlineStatus(onlineStatus);
                        }
                        friendViewHolder.setName(userName);
                        friendViewHolder.setStatus(userStatus);
                        friendViewHolder.setUserImage(userThumb,getContext());

                        friendViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence[] options =new CharSequence[]{"Open Profile","Send Message"};
                                AlertDialog.Builder builder=new AlertDialog.Builder(getContext());

                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if(i==0){
                                            Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("userId",userId);
                                            profileIntent.putExtra("name",userName);
                                            startActivity(profileIntent);
                                        }
                                        if(i==1){
                                            Intent profileIntent=new Intent(getContext(),ChatActivity.class);
                                            profileIntent.putExtra("userId",userId);
                                            profileIntent.putExtra("name",userName);
                                            startActivity(profileIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });
            }
        };
        friendRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
    public static class FriendViewHolder extends RecyclerView.ViewHolder{

        View view;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);

            view=itemView;
        }
        public void setName(String name){
            TextView friendName=view.findViewById(R.id.singleUserName);
            friendName.setText(name);
        }
        public void setStatus(String status){
            TextView friendName=view.findViewById(R.id.singleUserStatus);
            friendName.setText(status);
        }
        public void setUserImage(String thumb_image, Context applicationContext){
            CircleImageView userPic=view.findViewById(R.id.singleUserImage);
            Picasso.get().load(thumb_image).placeholder(R.drawable.profilepic).into(userPic);
        }
        public void setOnlineStatus(String isOnline){
            ImageView imageView=view.findViewById(R.id.userOnlineIcon);
            if(isOnline.equals("True")){
                imageView.setVisibility(View.VISIBLE);
            }else{
                imageView.setVisibility(View.INVISIBLE);
            }
        }
    }
}