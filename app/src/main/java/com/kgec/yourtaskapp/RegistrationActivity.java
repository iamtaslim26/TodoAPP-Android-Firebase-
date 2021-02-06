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

public class RegistrationActivity extends AppCompatActivity {
    private EditText etEmail,etPassword;
    private Button register_btn;
    private TextView login_option;

    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        etEmail=findViewById(R.id.registration_email);
        etPassword=findViewById(R.id.registration_password);
        register_btn=findViewById(R.id.register_btn);
        login_option=findViewById(R.id.login_link);

        loadingbar=new ProgressDialog(this);


        mAuth=FirebaseAuth.getInstance();

        login_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email=etEmail.getText().toString();
                String password=etPassword.getText().toString();

                if (TextUtils.isEmpty(email)){
                    loadingbar.dismiss();
                    Toast.makeText(RegistrationActivity.this, "Please Enter the email", Toast.LENGTH_LONG).show();
                }
                else if (TextUtils.isEmpty(password)){
                    loadingbar.dismiss();

                    Toast.makeText(RegistrationActivity.this, "Please Enter the password", Toast.LENGTH_LONG).show();
                }
                else if (password.length()<6){
                    loadingbar.dismiss();

                    Toast.makeText(RegistrationActivity.this, "Enter the password more than 6 digits", Toast.LENGTH_LONG).show();
                }
                else {

                    loadingbar.setTitle("Registarion Account");
                    loadingbar.setMessage("Processing. . . .");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();

                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){

                                Toast.makeText(RegistrationActivity.this, "Succesfull. . .", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(RegistrationActivity.this,HomeActivity.class));

                            }
                            else {
                                loadingbar.dismiss();
                                String message=task.getException().getMessage();
                                Toast.makeText(RegistrationActivity.this, "Error   "+message, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }
            }
        });
    }


}