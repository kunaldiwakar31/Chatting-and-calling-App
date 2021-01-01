package com.application.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.HttpAuthHandler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    FirebaseUser currentUser;

    private CircleImageView displayImage;
    private TextView displayName;
    private TextView displayStatus;
    private Button changeStatusButton;
    private Button changeImageButton;
    private static final int GALLERY_PICK=1;
    private StorageReference imageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        displayName=findViewById(R.id.settingsName);
        displayImage=findViewById(R.id.settingsPic);
        displayStatus=findViewById(R.id.settingsStatus);
        changeStatusButton=findViewById(R.id.settingsChangeStatus);
        changeImageButton=findViewById(R.id.settingsChangeImage);
        imageStorage= FirebaseStorage.getInstance().getReference();

        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        String currentUid=currentUser.getUid();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        databaseReference.keepSynced(true);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                    String name = snapshot.child("name").getValue().toString();
                    final String image = snapshot.child("image").getValue().toString();
                    String status = snapshot.child("status").getValue().toString();
                    String thumb_image = snapshot.child("thumb_image").getValue().toString();

                displayName.setText(name);
                displayStatus.setText(status);
                if(!image.equals("default")){
                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.profilepic).into(displayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
                            Picasso.get().load(image).placeholder(R.drawable.profilepic).into(displayImage);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        changeStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent statusIntent=new Intent(SettingsActivity.this,StatusActivity.class);
                String status=displayStatus.getText().toString();
                statusIntent.putExtra("status",status);
                startActivity(statusIntent);

            }
        });
        changeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent=new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"SELECT IMAGE"),GALLERY_PICK);

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK) {
            Uri imageUri=data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500,500)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                File thumbFilePath=new File(resultUri.getPath());
                final byte[] thumb_byte;

                Bitmap compressedImageBitmap = null;
                try {
                    compressedImageBitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumbFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumb_byte = baos.toByteArray();

                //    Bitmap thumbBitmap=new Compressor.compress()

                final String currentUid=currentUser.getUid();

                final StorageReference storageReference=imageStorage.child("profile_pics").child(currentUid+".jpg");
                final StorageReference thumb_filePath=imageStorage.child("profile_pics").child("thumbs").child(currentUid+".jpg");
                storageReference.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            UploadTask uploadTask = thumb_filePath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbtask) {
                                    if(thumbtask.isSuccessful()){

                                        task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                final String downloadUrl=String.valueOf(uri);
                                                databaseReference.child("image").setValue(downloadUrl);
                                            }
                                        });
                                        thumbtask.getResult().getStorage().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                final String downloadThumbUrl=String.valueOf(uri);
                                                databaseReference.child("thumb_image").setValue(downloadThumbUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(SettingsActivity.this,"Uploaded",Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        });
                                    }
                                    else{
                                        Toast.makeText(SettingsActivity.this,"Failed to upload thumbnail image",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }else {
                            Toast.makeText(SettingsActivity.this,"Failed to Upload",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(20);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

}