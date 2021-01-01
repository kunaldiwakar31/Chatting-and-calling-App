package com.application.chattingapp;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;


import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<MessageClass> mMessageList;
    private final static int VIEW_TYPE_SENT=1;
    private final static int VIEW_TYPE_RECIEVE=2;
    String currentUserId;
    String fromUser;
    private Context mContext;

    public MessageAdapter(List<MessageClass> mMessageList,Context context){
        this.mMessageList=mMessageList;
        this.mContext=context;
    }

    @Override
    public int getItemViewType(int position) {

        MessageClass messageClass=mMessageList.get(position);
        currentUserId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        fromUser=messageClass.getFrom();
        if(currentUserId.equals(fromUser)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECIEVE;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType==VIEW_TYPE_SENT){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_list_sender, parent, false);
            return new MessageViewHolder(view);
        }else if(viewType==VIEW_TYPE_RECIEVE){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.message_single_list, parent, false);
            return new MessageViewHolder(view);
        }
        return null;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position) {
        MessageClass message=mMessageList.get(position);

        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(message.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String image=snapshot.child("thumb_image").getValue().toString();
                Picasso.get().load(image).placeholder(R.drawable.profilepic2).into(holder.profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Log.i("SIZE", String.valueOf(mMessageList.size())+ "/"+position);

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(message.getTime());
        String date = DateFormat.format("HH:mm", cal).toString();

        if(message.getType().equals("image")){
            holder.messageText.setVisibility(View.GONE);
            holder.messageTime.setVisibility(View.GONE);
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.messageTimeImage.setVisibility(View.VISIBLE);

            holder.messageTimeImage.setText(date);
            Uri imageUri= Uri.parse(message.getMessage());
            Picasso.get().load(imageUri).placeholder(R.drawable.imageicon)
                    .into(holder.messageImage);

        }else {
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageTime.setVisibility(View.VISIBLE);
            holder.messageImage.setVisibility(View.GONE);
            holder.messageTimeImage.setVisibility(View.GONE);

            holder.messageTime.setText(date);
            holder.messageText.setText(message.getMessage());
        }

        holder.messageText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder alertDialog=new AlertDialog.Builder(mContext);
                alertDialog.setTitle("Are you Sure?");
                alertDialog.setMessage("Do you want to delete the message?");
                alertDialog.setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteMessage(position);
                            }
                        });
                alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                alertDialog.show();
                return true;
            }
        });
    }


    public static class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public CircleImageView profileImage;
        public TextView messageTime;
        public TextView messageTimeImage;
        public ImageView messageImage;
        public TextView messageSeenText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText=itemView.findViewById(R.id.messageText);
            profileImage=itemView.findViewById(R.id.messageProfilePic);
            messageTime=itemView.findViewById(R.id.messageTime);
            messageTimeImage=itemView.findViewById(R.id.messageTimeImage);
            messageImage=itemView.findViewById(R.id.messageImage);

        }
    }

    private void deleteMessage(int position) {
        MessageClass message=mMessageList.get(position);
        long time=message.getTime();

        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Chats");

        Query query=reference.orderByChild("time").equalTo(time);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    snapshot1.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        DatabaseReference reference1=FirebaseDatabase.getInstance().getReference("Chats");
        Query query1=reference1.orderByChild("time").equalTo(time);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    snapshot1.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mMessageList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}
