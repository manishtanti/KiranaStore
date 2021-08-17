package com.example.kiranastore;

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

import com.example.kiranastore.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends AppCompatActivity {

    private EditText name,email,password,phoneNumber;
    private Button register;
    private TextView goToLoginPage;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        name = findViewById(R.id.register_name);
        email = findViewById(R.id.register_email);
        password = findViewById(R.id.register_password);
        phoneNumber = findViewById(R.id.register_phone_no);
        register = findViewById(R.id.register_to_db);
        goToLoginPage = findViewById(R.id.go_to_login_page);
        pd = new ProgressDialog(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        goToLoginPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txtName = name.getText().toString();
                String txtEmail = email.getText().toString();
                String txtPassword = password.getText().toString();
                String txtPhoneNumber = phoneNumber.getText().toString();

                if(TextUtils.isEmpty(txtName.trim()) || TextUtils.isEmpty(txtEmail.trim()) || TextUtils.isEmpty(txtPassword.trim())||TextUtils.isEmpty(txtPhoneNumber.trim()) || txtPhoneNumber.trim().charAt(0)=='0'){
                    Toast.makeText(getApplicationContext(),getString(R.string.empty_credentials),Toast.LENGTH_SHORT).show();
                }  else if(txtPassword.length()<6){
                    Toast.makeText(getApplicationContext(), getString(R.string.password_error),Toast.LENGTH_SHORT).show();
                } else if(!TextUtils.isDigitsOnly(txtPhoneNumber) || txtPhoneNumber.length()!=10){
                    Toast.makeText(RegisterActivity.this, getString(R.string.phone_error), Toast.LENGTH_SHORT).show();
                }else if(txtPhoneNumber.charAt(0)==0){
                    Toast.makeText(RegisterActivity.this, getString(R.string.phone_leading_zero), Toast.LENGTH_SHORT).show();
                }
                else{
                    registerUser(txtName.trim(),txtEmail.trim(),txtPassword.trim(),txtPhoneNumber);
                }
            }
        });
    }



    private void registerUser(String txtName, String txtEmail, String txtPassword,String txtPhoneNumber) {

        pd.setTitle(getString(R.string.please_wait));
        pd.show();
        mAuth.createUserWithEmailAndPassword(txtEmail,txtPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                User user = new User(false,txtEmail,mAuth.getCurrentUser().getUid(),"default",txtName,txtPhoneNumber);
                databaseReference.child("Users").child(mAuth.getCurrentUser().getUid()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(), getString(R.string.registration_successfull_message),Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this,LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            finish();
                        } else{
                            pd.dismiss();
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}