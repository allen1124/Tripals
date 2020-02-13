package com.hku.tripals;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private EditText displayName;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private Button createAccountButton;
    private Button loginButton;
    private ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        displayName = findViewById(R.id.display_name_editText);
        email = findViewById(R.id.email_editText);
        password = findViewById(R.id.password_editText);
        confirmPassword = findViewById(R.id.confirm_password_editText);
        createAccountButton = findViewById(R.id.create_account_button);
        loginButton = findViewById(R.id.start_login_button);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(displayName.getText().toString().matches("") || email.getText().toString().matches("")
                || password.getText().toString().matches("") || confirmPassword.getText().toString().matches("")){
                    Toast.makeText(SignUpActivity.this,
                            R.string.not_complete_message, Toast.LENGTH_SHORT).show();
                    if(displayName.getText().toString().matches(""))
                        displayName.setError(getText(R.string.required));
                    if(email.getText().toString().matches(""))
                        email.setError(getText(R.string.required));
                    if(password.getText().toString().matches(""))
                        password.setError(getText(R.string.required));
                    if(confirmPassword.getText().toString().matches(""))
                        confirmPassword.setError(getText(R.string.required));
                }else if(!password.getText().toString().matches(confirmPassword.getText().toString())){
                    confirmPassword.setError(getText(R.string.confirm_password_message));
                }else{
                    createAccount(email.getText().toString(), password.getText().toString());
                }
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                SignUpActivity.this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    private void createAccount(String email, String password){
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            currentUser = mAuth.getCurrentUser();
                            updateUserDisplayName(currentUser, displayName.toString());
                            currentUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(SignUpActivity.this, "Sign Up successfully! Please check your email for verification.",
                                                Toast.LENGTH_LONG).show();
                                        clearForm();
                                    }else{
                                        Toast.makeText(SignUpActivity.this, task.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                            mAuth.signOut();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressBar.setVisibility(View.GONE);
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUserDisplayName(FirebaseUser user, String displayName){
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();
        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });
    }

    private void clearForm(){
        displayName.setText("");
        email.setText("");
        password.setText("");
        confirmPassword.setText("");
    }
}
