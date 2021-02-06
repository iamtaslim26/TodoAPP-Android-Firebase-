package com.kgec.yourtaskapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.text.DateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private FloatingActionButton fab_btn;

    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference mDatabase;
    private String userId;
    private RecyclerView recyclerView;

    private CircleImageView imageView;

    private EditText titleupt,noteupt;
    private Button delete_btn,update_btn;
    private String title1,note1,post_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mToolbar=findViewById(R.id.toolbar_home);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Your Task App");





        recyclerView=findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        mAuth=FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();
        userId=firebaseUser.getUid();

        mDatabase=FirebaseDatabase.getInstance().getReference().child("Your Task").child(userId);

        mDatabase.keepSynced(true);

        imageView=findViewById(R.id.custom_profile_image);



        fab_btn=findViewById(R.id.fab);

        fab_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               AlertDialog.Builder builder=new AlertDialog.Builder(HomeActivity.this);

               LayoutInflater layoutInflater=LayoutInflater.from(HomeActivity.this);

               View view=layoutInflater.inflate(R.layout.custominput,null);

               builder.setView(view);

               final AlertDialog dialog=builder.create();

               final EditText mTitle=view.findViewById(R.id.etd_title);
               final EditText mNote=view.findViewById(R.id.etd_note);
               Button btn_save=view.findViewById(R.id.btn_save);


               btn_save.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {

                      String title=mTitle.getText().toString();
                      String note=mNote.getText().toString();

                       if (TextUtils.isEmpty(title)){

                           Toast.makeText(HomeActivity.this, "Please fill details ", Toast.LENGTH_SHORT).show();
                       }
                       else if (TextUtils.isEmpty(note)){

                           Toast.makeText(HomeActivity.this, "Please fill the details", Toast.LENGTH_SHORT).show();
                       }
                       else {

                           String id=mDatabase.push().getKey();

                           String date= DateFormat.getDateInstance().format(new Date());



                           Data data=new Data(title,note,date,id);
                           mDatabase.child(id).setValue(data);

                           Toast.makeText(HomeActivity.this, "Data Insert", Toast.LENGTH_SHORT).show();

                           dialog.dismiss();



                       }

                   }
               });


               dialog.show();

            }
        });

        mDatabase.child("Details").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()&&dataSnapshot.hasChild("image")){

                    String image=dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(image).placeholder(R.drawable.profile_image).into(imageView);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Data,myViewHolder>adapter=new FirebaseRecyclerAdapter<Data, myViewHolder>(
                Data.class,
                R.layout.item_data,
                myViewHolder.class,
                mDatabase



        ) {
            @Override
            protected void populateViewHolder(final myViewHolder myViewHolder, final Data model, final int position) {

                myViewHolder.setTitle(model.getTitle());
                myViewHolder.setNote(model.getNote());
                myViewHolder.setDate(model.getDate());


                myViewHolder.myView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        
                        post_key=getRef(position).getKey();

                        title1=model.getTitle();
                        note1=model.getNote();

                        UpdateTask();

                    }
                });

            }
        };

        recyclerView.setAdapter(adapter);





    }

    private void UpdateTask() {
        AlertDialog.Builder builder=new AlertDialog.Builder(HomeActivity.this);
        LayoutInflater layoutInflater=LayoutInflater.from(HomeActivity.this);

        View view=layoutInflater.inflate(R.layout.updateanddeleteinput,null);
        builder.setView(view);


        final AlertDialog dialog=builder.create();

        titleupt=view.findViewById(R.id.etd_title_update);
        noteupt=view.findViewById(R.id.etd_note_update);

        titleupt.setText(title1);
        noteupt.setText(note1);



        update_btn=view.findViewById(R.id.btn_update);
        delete_btn=view.findViewById(R.id.btn_delete);




        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                title1=titleupt.getText().toString();
                note1=noteupt.getText().toString();

                String date= DateFormat.getDateInstance().format(new Date());



                Data data=new Data(title1,note1,date,post_key);
                mDatabase.child(post_key).setValue(data);

                dialog.dismiss();

            }
        });


        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDatabase.child(post_key).removeValue();

                dialog.dismiss();

            }
        });

        //here add delete options

        dialog.show();






    }

    public static class myViewHolder extends RecyclerView.ViewHolder {

        View myView;
        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            myView=itemView;
        }

        public void setTitle(String title){

            TextView mTitle=myView.findViewById(R.id.txttitle1);
            mTitle.setText(title);
        }

        public void setNote(String note){

            TextView mNote=myView.findViewById(R.id.txtnote1);
            mNote.setText(note);
        }

        public void setDate(String date){

            TextView mDAte=myView.findViewById(R.id.txtdate);
            mDAte.setText(date);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu,menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.logout:
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;

            case R.id.settings_toolbar:

                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                break;
        }




        return super.onOptionsItemSelected(item);

    }
}