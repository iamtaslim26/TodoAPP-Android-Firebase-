package com.kgec.yourtaskapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private CircleImageView imageView;
    private EditText name_set, address_set;
    private Button Update_btn;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference settingsRef;
    private StorageReference ImageRef;
    private String currentUserId;


    final static int gallerypick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        imageView = findViewById(R.id.set_profile_image);
        name_set = findViewById(R.id.set_user_name);
        address_set = findViewById(R.id.set_address);
        Update_btn = findViewById(R.id.set_update);

        mToolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Settings");


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserId = currentUser.getUid();


        settingsRef = FirebaseDatabase.getInstance().getReference().child("Your Task").child(currentUserId);
        ImageRef= FirebaseStorage.getInstance().getReference().child("Profile Pictures");

        Update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UpdateSettings();
            }
        });

        RetriveUserInfo();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,gallerypick);



            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==gallerypick && resultCode==RESULT_OK && data!=null){

            Uri ImageUri=data.getData();

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);


        }
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result=CropImage.getActivityResult(data);

            if (resultCode==RESULT_OK){

                Uri resultUri=result.getUri();


                StorageReference filepath=ImageRef.child(currentUserId+".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){

                            Toast.makeText(SettingsActivity.this, "Image is saved in firebase storage", Toast.LENGTH_SHORT).show();

                            final String downloadUrl=task.getResult().getDownloadUrl().toString();
                            //now add into the realtime database

                            settingsRef.child("Details").child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        Toast.makeText(SettingsActivity.this, "Image is stored into the real time database", Toast.LENGTH_SHORT).show();
                                    }
                                    else {

                                        String e=task.getException().getMessage();

                                        Toast.makeText(SettingsActivity.this, "Failed. . ."+e, Toast.LENGTH_SHORT).show();


                                    }

                                }
                            });

                        }
                        else {

                            String e=task.getException().getMessage();

                            Toast.makeText(SettingsActivity.this, "Failed. . ."+e, Toast.LENGTH_SHORT).show();
                        }

                    }
                });


            }
            else {

                Toast.makeText(this, "Image cannot be cropped. . . .", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void RetriveUserInfo() {

        settingsRef.child("Details").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")){

                    String userImage=dataSnapshot.child("image").getValue().toString();

                    String username=dataSnapshot.child("name").getValue().toString();
                    String useraddress=dataSnapshot.child("address").getValue().toString();

                    name_set.setText(username);
                    address_set.setText(useraddress);

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(imageView);



                }
                else if (dataSnapshot.exists()&&dataSnapshot.hasChild("name")){

                    String username=dataSnapshot.child("name").getValue().toString();
                    String useraddress=dataSnapshot.child("address").getValue().toString();

                    name_set.setText(username);
                    address_set.setText(useraddress);
                }
                else {

                    Toast.makeText(SettingsActivity.this, "Please Enter name", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    private void UpdateSettings() {

        String name=name_set.getText().toString();
        String address=address_set.getText().toString();

        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please Enter the name. . . ", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(address)){
            Toast.makeText(this, "Please Enter the password. . . ", Toast.LENGTH_SHORT).show();
        }

        else {


            HashMap<String,Object>map=new HashMap<>();
            map.put("name",name);
            map.put("address",address);

            settingsRef.child("Details").updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){

                        Intent intent=new Intent(getApplicationContext(),HomeActivity.class);
                        startActivity(intent);
                        Toast.makeText(SettingsActivity.this, "Succesfull. .. . ", Toast.LENGTH_SHORT).show();
                    }
                    else {

                        String e=task.getException().getMessage();
                        Toast.makeText(SettingsActivity.this, "Failed. ..    "+e, Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }

    }
}
