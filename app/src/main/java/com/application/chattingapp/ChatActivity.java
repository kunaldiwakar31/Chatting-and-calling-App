package com.application.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.google.firebase.database.DatabaseReference.*;

public class ChatActivity extends AppCompatActivity {

    private Toolbar chatToolbar;
    private DatabaseReference rootReference;
    private StorageReference imageStorage;
    private FirebaseUser currentUser;
    private CircleImageView chatProfilePic;
    private EditText chatMessageText;
    private TextView chatUserName;
    private TextView chatLastSeen;
    private ImageButton chatAddButton;
    private ImageButton chatSendButton;
    private RecyclerView messageList;
    private List<MessageClass> messageArrayList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private String userID;
    private static final int TOTAL_ITEMS_TO_LOAD=10;
    private int currentPage=1;
    private SwipeRefreshLayout refreshLayout;
    private int itemPosition=0;
    private String lastKey="";
    private String prevKey="";
    private static final int REQUEST_CALL=1;
    private static final int GALLERY_PICK=1;
    private String currentUserId;
    private String number="tel:";

    ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userID= getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("name");

        chatToolbar = findViewById(R.id.chatBarLayout);
        setSupportActionBar(chatToolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.back_icon);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        ChatActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(actionbarView);

        chatProfilePic = findViewById(R.id.chatProfilePic);
        chatUserName = findViewById(R.id.chatUserName);
        chatLastSeen = findViewById(R.id.chatLastSeen);
        chatMessageText = findViewById(R.id.chatMessageText);
        chatAddButton = findViewById(R.id.chataddButton);
        chatSendButton = findViewById(R.id.chatSendButton);
        refreshLayout=findViewById(R.id.chatRefreshLayout);

        rootReference = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        imageStorage= FirebaseStorage.getInstance().getReference();

        messageList=findViewById(R.id.messageList);
        messageAdapter=new MessageAdapter(messageArrayList,ChatActivity.this);
        linearLayoutManager=new LinearLayoutManager(this);
        messageList.setHasFixedSize(true);
        messageList.setLayoutManager(linearLayoutManager);
        messageList.setAdapter(messageAdapter);

        android.content.Context context = this.getApplicationContext();

        currentUserId=currentUser.getUid();

        loadMessages();

        chatUserName.setText(userName);
        rootReference.child("Users").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String Online = snapshot.child("online").getValue().toString();
                String thumb_image = snapshot.child("thumb_image").getValue().toString();

                if (Online.equals("True")) {
                    chatLastSeen.setText("online");
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();
                    long lastSeen = Long.parseLong(Online);
                    String lastTimeAgo = getTimeAgo.getTimeAgo(lastSeen, getApplicationContext());
                    chatLastSeen.setText(lastTimeAgo);
                }
                Picasso.get().load(thumb_image).placeholder(R.drawable.chatprofilepic2).into(chatProfilePic);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        rootReference.child("ChatUser").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(userID)) {
                    Map<String, Object> chatAddMap = new HashMap<String, Object>();
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                    chatAddMap.put("seen",false);

                    Map<String, Object> chatUserMap = new HashMap<String, Object>();
                    chatUserMap.put("ChatUser/" + currentUserId + "/" + userID, chatAddMap);
                    chatUserMap.put("ChatUser/" + userID + "/" +currentUserId, chatAddMap);

                    rootReference.updateChildren(chatUserMap, new CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Log.i("CHAT LOG", error.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        chatAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);

            }
        });

        chatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
            private void sendMessage() {
                String message=chatMessageText.getText().toString();

                if(!TextUtils.isEmpty(message)) {

                    DatabaseReference user_message_push= rootReference.child("Chats").push();
                    String pushId=user_message_push.getKey();

                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("message", message);
                    messageMap.put("seen", false);
                    messageMap.put("time", ServerValue.TIMESTAMP);
                    messageMap.put("type", "text");
                    messageMap.put("from",currentUserId);
                    messageMap.put("to",userID);

                    Map<String, Object> messageUserMap=new HashMap<>();
                    messageUserMap.put("Chats"+"/"+pushId,messageMap);

                    chatMessageText.setText("");

                    rootReference.updateChildren(messageUserMap, new CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error != null) {
                                Log.i("CHAT LOG", error.getMessage());
                            }
                        }
                    });

                }
            }
        });
        seenMessage();
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                itemPosition=0;
                loadMoreMessages();
            }

        });

    }

    public void seenMessage(){

        seenListener = rootReference.child("Chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()) {
                    MessageClass message=snapshot1.getValue(MessageClass.class);
                    if(message.getTo().equals(currentUserId) && message.getFrom().equals(userID)){
                        HashMap<String,Object> map=new HashMap<>();
                        map.put("seen",true);
                        snapshot1.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

        rootReference.child("Chats").removeEventListener(seenListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){
            Uri imageUri=data.getData();

            DatabaseReference user_message_push= rootReference.child("Chats").push();
            final String pushId=user_message_push.getKey();
            final StorageReference storageReference=imageStorage.child("message_images").child(pushId+".jpg");
            storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @SuppressLint("ShowToast")
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){

                        final HashMap<String, Object> userMap = new HashMap<>();
                        userMap.put("message", "default");
                        userMap.put("seen", false);
                        userMap.put("time", ServerValue.TIMESTAMP);
                        userMap.put("type", "image");
                        userMap.put("from", currentUserId);
                        userMap.put("to",userID);

                        Map<String, Object> messageUserMap=new HashMap<>();
                        messageUserMap.put("Chats"+"/"+pushId,userMap);

                        chatMessageText.setText("");
                        rootReference.updateChildren(messageUserMap, new CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error != null) {
                                    Log.i("CHAT LOG", error.getMessage());
                                }
                            }
                        });
                        task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                Log.i("IMAGE",String.valueOf(uri));

                                rootReference.child("Chats").child(pushId).child("message").setValue(String.valueOf(uri));
                            }
                        });

                    }else{
                        Toast.makeText(ChatActivity.this,"Error in Loading Image!!",Toast.LENGTH_SHORT);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.chat_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.chatCall){
            makeCall();
        }else if(item.getItemId()==android.R.id.home){
            finish();
        }
        return true;
    }

    private void makeCall() {

        rootReference.child("Users").child(userID).child("phoneNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(number.equals("tel:")) {
                    number += String.valueOf(snapshot.getValue());
                }
                Log.i("NUMBER",String.valueOf(snapshot.getValue()));
                if(ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(ChatActivity.this,new String[] {Manifest.permission.CALL_PHONE},REQUEST_CALL);
                }else{
                    Log.i("DIAL",number);
                    startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(number)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_CALL){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                makeCall();
            }else{
                Toast.makeText(ChatActivity.this,"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadMoreMessages() {
        DatabaseReference ChatsReference= rootReference.child("Chats");
        Query messageQuery=ChatsReference.orderByKey().endAt(lastKey).limitToLast(TOTAL_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageClass messageClass=snapshot.getValue(MessageClass.class);

                if(!prevKey.equals(snapshot.getKey())) {
                    messageArrayList.add(itemPosition++,messageClass);

                }else{
                    prevKey=lastKey;

                }
                if(itemPosition==1){
                    lastKey= snapshot.getKey();
                }

                messageAdapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
                linearLayoutManager.scrollToPositionWithOffset(TOTAL_ITEMS_TO_LOAD,0);
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
    }

    private void loadMessages() {

        messageArrayList=new ArrayList<>();
        DatabaseReference messageReference= rootReference.child("Chats");
        messageReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageArrayList.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    MessageClass message=snapshot1.getValue(MessageClass.class);

                    assert message != null;
                    if((message.getFrom().equals(currentUserId) && message.getTo().equals(userID)) ||
                            (message.getFrom().equals(userID) && message.getTo().equals(currentUserId))) {
                        messageArrayList.add(message);
                    }
                    messageAdapter=new MessageAdapter(messageArrayList,ChatActivity.this);
                    messageAdapter.notifyDataSetChanged();

                    messageList.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}