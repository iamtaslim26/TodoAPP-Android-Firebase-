package com.kgec.yourtaskapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText etEmail,etPassword;
    private Button login_btn;
    private TextView register_option;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        etEmail=findViewById(R.id.login_email);
        etPassword=findViewById(R.id.login_password);
        login_btn=findViewById(R.id.login_btn);
        register_option=findViewById(R.id.register_link);

        mAuth=FirebaseAuth.getInstance();
        loadingbar=new ProgressDialog(this);
        firebaseUser=mAuth.getCurrentUser();


        register_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(),RegistrationActivity.class));
            }
        });

        if (firebaseUser!=null){

            Intent intent=new Intent(getApplicationContext(),HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=etEmail.getText().toString();
                String password=etPassword.getText().toString();

                if (TextUtils.isEmpty(email)){
                    loadingbar.dismiss();
                    Toast.makeText(MainActivity.this, "Please Enter the email", Toast.LENGTH_LONG).show();
                }
                else if (TextUtils.isEmpty(password)){
                    loadingbar.dismiss();

                    Toast.makeText(MainActivity.this, "Please Enter the password", Toast.LENGTH_LONG).show();
                }
                else {
                    loadingbar.setTitle("Login Account");
                    loadingbar.setMessage("Processing. . . .");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();

                    mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                loadingbar.show();

                                Toast.makeText(MainActivity.this, "Succesfull. . .", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(MainActivity.this,HomeActivity.class));

                            }
                            else {
                                loadingbar.dismiss();
                                String message=task.getException().getMessage();
                                Toast.makeText(MainActivity.this, "Error   "+message, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }


            }
        });


    }
}