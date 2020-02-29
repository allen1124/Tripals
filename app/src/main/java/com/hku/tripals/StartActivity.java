package com.hku.tripals;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StartActivity extends AppCompatActivity {

    private static final String TAG = "StartActivity";
    private static final int GOOGLE_SIGN_IN = 1111;
    private SignInButton signInButton;
    private Button emailSignInButton;
    private Button noLoginButton;
    private Button loginButton;
    private ProgressBar progressBar;
    private RelativeLayout horizontalDivider;
    private TextView agreement1;
    private TextView agreement2;
    private TextView agreement3;
    private TextView agreement4;

    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        signInButton = findViewById(R.id.sign_in_button);
        TextView signInButtonTextView = (TextView) signInButton.getChildAt(0);
        signInButtonTextView.setText(R.string.sign_in_with_google);
        emailSignInButton = findViewById(R.id.email_signin_button);
        noLoginButton = findViewById(R.id.no_login_button);
        loginButton = findViewById(R.id.start_login_button);
        progressBar = findViewById(R.id.progressBar);
        horizontalDivider = findViewById(R.id.horizontal_divider);
        progressBar.setVisibility(View.INVISIBLE);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInGoogle();
            }
        });
        emailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, SignUpActivity.class));
                StartActivity.this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
        noLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Proceed with no login");
                Toast.makeText(StartActivity.this,
                        "Proceeds without login.", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.VISIBLE);
                signInButton.setEnabled(false);
                emailSignInButton.setEnabled(false);
                noLoginButton.setEnabled(false);
                loginButton.setEnabled(false);
                goToMainActivity(0);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(StartActivity.this, LoginActivity.class));
                StartActivity.this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
        agreement1 = findViewById(R.id.agreement_textView);
        agreement2 = findViewById(R.id.terms_textView);
        agreement3 = findViewById(R.id.and_textView);
        agreement4 = findViewById(R.id.policy_textView);
        agreement2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, TermsActivity.class);
                intent.putExtra("title", "Term of Service");
                startActivity(intent);
                StartActivity.this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
        agreement4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this, TermsActivity.class);
                intent.putExtra("title", "Privacy Policy");
                startActivity(intent);
                StartActivity.this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser != null){
            signInButton.setVisibility(View.GONE);
            emailSignInButton.setVisibility(View.GONE);
            noLoginButton.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
            horizontalDivider.setVisibility(View.GONE);
            agreement1.setVisibility(View.GONE);
            agreement2.setVisibility(View.GONE);
            agreement3.setVisibility(View.GONE);
            agreement4.setVisibility(View.GONE);
            Log.d(TAG, "onStart: Login-ed already");
            proceed(currentUser);
        }
    }

    private void goToMainActivity(long waitTime){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
            }
        }, waitTime);
        Log.d(TAG, "goToMainActivity: wait and, start MainActivity");
    }

    public void signInGoogle() {
        Log.d(TAG, "signInGoogle: called");
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if(account != null)
                    firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential: success");
                            currentUser = mAuth.getCurrentUser();
                            proceed(currentUser);
                        } else {
                            // If sign in fails, display a message to the user.
                            progressBar.setVisibility(View.INVISIBLE);
                            Log.w(TAG, "signInWithCredential: failure", task.getException());
                            Snackbar.make(findViewById(R.id.constraint_layout), "Authentication Failed, proceeds without login.", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void proceed(FirebaseUser user){
        if(user.isEmailVerified()){
            //if User has not profile, proceed to Create Profile Page, else MainPage
            database.collection("user-profile").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot profileDoc = task.getResult();
                        progressBar.setVisibility(View.INVISIBLE);
                        if (profileDoc.exists()) {
                            Log.d(TAG, "profileDoc.exists: User profile created before");
                            goToMainActivity(0);
                        }else {
                            Log.d(TAG, "profileDoc.!exists: User profile not created");
                            startActivity(new Intent(StartActivity.this, ProfileActivity.class));
                        }
                        finish();
                    } else {
                        Log.d(TAG, "Failed with: ", task.getException());
                    }
                }
            });
        }else{
            mAuth.signOut();
            Toast.makeText(StartActivity.this, R.string.email_not_verified,
                    Toast.LENGTH_LONG).show();
        }
    }
}
