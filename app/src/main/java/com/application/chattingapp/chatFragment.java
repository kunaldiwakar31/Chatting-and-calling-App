package com.application.chattingapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Currency;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class chatFragment extends Fragment {

    private RecyclerView chatRecyclerView;
    private DatabaseReference chatReference;
    private DatabaseReference userReference;
    private DatabaseReference chatUserReference;
    private String currentUserId;
    private View mView;
    private FirebaseAuth chatAuth;
    private TextView chatText;

    public chatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView=inflater.inflate(R.layout.fragment_chat, container, false);

        chatRecyclerView=mView.findViewById(R.id.chatRecyclerView);
        chatAuth=FirebaseAuth.getInstance();
        currentUserId=chatAuth.getCurrentUser().getUid();

        chatReference=FirebaseDatabase.getInstance().getReference().child("Chats");
        chatReference.keepSynced(true);
        chatUserReference=FirebaseDatabase.getInstance().getReference().child("ChatUser");
        chatUserReference.keepSynced(true);
        userReference=FirebaseDatabase.getInstance().getReference().child("Users");
        userReference.keepSynced(true);

        chatRecyclerView.setHasFixedSize(true);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ChatUser> options =
                new FirebaseRecyclerOptions.Builder<ChatUser>()
                        .setQuery(chatUserReference.child(currentUserId).orderByChild("timestamp"), ChatUser.class)
                        .build();
            FirebaseRecyclerAdapter<ChatUser, chatViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<ChatUser, chatViewHolder>(options) {
                @NonNull
                @Override
                public chatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.singleuserlayout,parent, false);
                    return new chatViewHolder(view);
                }

                @Override
                protected void onBindViewHolder(@NonNull final chatViewHolder chatViewHolder, int i, @NonNull final ChatUser message) {

                    final String userId=getRef(i).getKey();

//                    Toast.makeText(getContext(),userId,Toast.LENGTH_SHORT).show();
                    final Query lastMessageQuery = chatReference.limitToLast(1);
                    lastMessageQuery.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            try {
                                String data = Objects.requireNonNull(snapshot.child("message").getValue()).toString();
                                chatViewHolder.setMessage(data, message.isSeen());
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    userReference.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                final String userName = snapshot.child("name").getValue().toString();
                                String userStatus = snapshot.child("status").getValue().toString();
                                String userThumb = snapshot.child("thumb_image").getValue().toString();
                                if (snapshot.hasChild("online")) {
                                    String onlineStatus = snapshot.child("online").getValue().toString();
                                    chatViewHolder.setOnlineStatus(onlineStatus);
                                }
                                chatViewHolder.setName(userName);
                                chatViewHolder.setStatus(userStatus);
                                chatViewHolder.setUserImage(userThumb, getContext());

                                chatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent profileIntent = new Intent(getContext(), ChatActivity.class);
                                        profileIntent.putExtra("userId", userId);
                                        profileIntent.putExtra("name", userName);
                                        startActivity(profileIntent);
                                    }
                                });
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            };

            chatRecyclerView.setAdapter(firebaseRecyclerAdapter);
            firebaseRecyclerAdapter.startListening();
    }

    public static class chatViewHolder extends RecyclerView.ViewHolder {
        View view;

        public chatViewHolder(@NonNull View itemView) {
            super(itemView);

            view=itemView;
        }
        public void setMessage(String message, boolean isSeen){

            TextView userStatusView = view.findViewById(R.id.singleUserStatus);
            userStatusView.setText(message);

            if(!isSeen){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }

        }
        public void setName(String name){
            TextView friendName=view.findViewById(R.id.singleUserName);
            friendName.setText(name);
        }
        public void setStatus(String status){
            TextView friendStatus=view.findViewById(R.id.singleUserStatus);
            friendStatus.setText(status);
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