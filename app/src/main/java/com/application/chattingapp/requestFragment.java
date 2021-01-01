package com.application.chattingapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


public class requestFragment extends Fragment {

    private View mView;
    private RecyclerView requestRecyclerView;
    private String currentUserId;
    private DatabaseReference requestReference;
    private DatabaseReference userReference;
    private DatabaseReference friendReference;
    private ImageButton acceptRequestButton;
    private ImageButton denyRequestButton;

    public requestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_request, container, false);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestRecyclerView = mView.findViewById(R.id.requestRecyclerView);
        requestReference = FirebaseDatabase.getInstance().getReference().child("friendRequest");
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        friendReference=FirebaseDatabase.getInstance().getReference().child("friends");

        requestRecyclerView.setHasFixedSize(true);
        requestRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Requests, RequestViewHolder> firebaseRecyclerAdapter;
        FirebaseRecyclerOptions<Requests> options =
                new FirebaseRecyclerOptions.Builder<Requests>()
                        .setQuery(requestReference.child(currentUserId).orderByChild("requestType").equalTo("friendRecieved"), Requests.class)
                        .build();

        firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Requests, RequestViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int i, @NonNull Requests requests) {

                final String userId = getRef(i).getKey();

                final ImageButton acceptRequestButton=requestViewHolder.itemView.findViewById(R.id.acceptRequestButton);
                final ImageButton denyRequestButton=requestViewHolder.itemView.findViewById(R.id.denyRequestButton);

                userReference.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        final String userName = snapshot.child("name").getValue().toString();
                        String userThumb = snapshot.child("thumb_image").getValue().toString();

                        requestViewHolder.setName(userName);
                        requestViewHolder.setUserImage(userThumb, getContext());
                        requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                profileIntent.putExtra("userId", userId);
                                profileIntent.putExtra("name", userName);
                                startActivity(profileIntent);
                            }
                        });
                        acceptRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final String currentDate= DateFormat.getDateTimeInstance().format(new Date());
                                friendReference.child(currentUserId).child(userId).child("Date").setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            friendReference.child(userId).child(currentUserId).child("Date").setValue(currentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        requestReference.child(currentUserId).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()) {
                                                                    requestReference.child(userId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                Toast.makeText(getContext(),"You Accepted the Request.",Toast.LENGTH_SHORT).show();
                                                                            }else{
                                                                                Toast.makeText(getContext(),"Failed to Accept request!! Please try again.",Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                                }else{
                                                                    Toast.makeText(getContext(),"Failed to Accept request!! Please try again.",Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }else{
                                                        Toast.makeText(getContext(),"Failed to Accept request!! Please try again.",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }else{
                                            Toast.makeText(getContext(),"Failed to Accept request!! Please try again.",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });

                        denyRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                requestReference.child(userId).child(currentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            requestReference.child(currentUserId).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(getContext(),"You Declined the Request",Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Toast.makeText(getContext(),"Failed to decline request user!! Please try again.",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }else {
                                            Toast.makeText(getContext(),"Failed to decline request user!! Please try again.",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user_request_layout,parent, false);
                return new RequestViewHolder(view);
            }
        };
        requestRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private class RequestViewHolder extends RecyclerView.ViewHolder{
        View view;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
        }
        public void setName(String name) {
            TextView friendName = view.findViewById(R.id.singleUserRequestName);
            friendName.setText(name);
        }

        public void setUserImage(String thumb_image, Context applicationContext) {
            CircleImageView userPic = view.findViewById(R.id.singleUserRequestImage);
            Picasso.get().load(thumb_image).placeholder(R.drawable.profilepic).into(userPic);
        }
    }
}
