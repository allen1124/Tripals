package com.hku.tripals;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText email;
    private EditText password;
    private ImageButton back;
    private Button loginButton;
    private Button forgotPasswordButton;
    private ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.login_progressBar);
        progressBar.setVisibility(View.GONE);
        back = findViewById(R.id.login_back_imageButton);
        email = findViewById(R.id.login_email_editText);
        password = findViewById(R.id.login_password_editText);
        forgotPasswordButton = findViewById(R.id.forgot_password_button);
        loginButton = findViewById(R.id.start_login_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!email.getText().toString().matches("") && !password.getText().toString().matches("")) {
                    progressBar.setVisibility(View.VISIBLE);

                    logIn(email.getText().toString(), password.getText().toString());
                }
                if(email.getText().toString().matches(""))
                    email.setError(getText(R.string.required));
                if(password.getText().toString().matches(""))
                    password.setError(getText(R.string.required));
            }
        });
        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!email.getText().toString().matches("")){
                    resetPassword(email.getText().toString());
                }else {
                    email.setError(getText(R.string.required));
                }
            }
        });
    }



    private void resetPassword(String emailAddress){
        mAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, R.string.reset_password_email_sent,
                                    Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Email sent.");
                        }else{
                            Toast.makeText(LoginActivity.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void logIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            currentUser = mAuth.getCurrentUser();
                            proceed(currentUser);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

    private void proceed(FirebaseUser user){
        if(user.isEmailVerified()){
            Log.d(TAG, "proceed: User email is verified.");
            database.collection("user-profile").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot profileDoc = task.getResult();
                        progressBar.setVisibility(View.GONE);
                        if (profileDoc.exists()) {
                            Log.d(TAG, "profileDoc.exists: User profile created before");
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        }else {
                            Log.d(TAG, "profileDoc.!exists: User profile not created");
                            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
                        }
                        finish();
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                    }
                }
            });
        }else{
            Log.d(TAG, "proceed: User email is not verified.");
            mAuth.signOut();
            progressBar.setVisibility(View.GONE);
            Toast.makeText(LoginActivity.this, R.string.email_not_verified,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
