package com.hku.tripals;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class TermsActivity extends AppCompatActivity {

    private static final String TAG = "TermsActivity";
    private String title;
    private TextView terms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        Toolbar toolbar = findViewById(R.id.terms_toolbar);
        terms = findViewById(R.id.content_textView);
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if(title.matches("Term of Service")){
            terms.setText(R.string.term_of_service);
        }else{
            terms.setText(R.string.privacy_policy);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        return true;
    }
}
