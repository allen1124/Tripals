package com.hku.tripals;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hku.tripals.adapter.EventAdapter;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.User;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";
    private User user;

    private ImageView avatar;
    private TextView gender;
    private TextView age;
    private TextView country;
    private TextView interest;
    private TextView bio;
    private TextView eventCreatedTitle;
    private TextView noJoinedEvent;
    private RecyclerView createdEvent;
    private RecyclerView joinedEvent;
    private LinearLayoutManager createdEventLayoutManager;
    private LinearLayoutManager joinedEventLayoutManager;
    private EventAdapter createdEventAdapter, joinedEventAdapter;
    private List<Event> createdEventList = new ArrayList<>();
    private List<Event> joinedEventList = new ArrayList<>();

    DateFormat df = new SimpleDateFormat("d/M/yyyy");

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user");
        avatar = findViewById(R.id.user_profile_avatar_imageView);
        Glide.with(this)
                .load(user.getAvatarImageUrl())
                .circleCrop()
                .into(avatar);
        gender = findViewById(R.id.user_gender_textView);
        age = findViewById(R.id.user_age_textView);
        country = findViewById(R.id.user_country_textView);
        interest = findViewById(R.id.user_interest_textView);
        bio = findViewById(R.id.user_bio_textView);
        eventCreatedTitle = findViewById(R.id.user_created_event_title_textView);
        noJoinedEvent = findViewById(R.id.no_joined_event_textView);
        noJoinedEvent.setVisibility(View.GONE);
        createdEvent = findViewById(R.id.user_created_event_RecyclerView);
        joinedEvent = findViewById(R.id.user_joined_event_RecyclerView);
        createdEventLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        joinedEventLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        createdEventAdapter = new EventAdapter(this);
        joinedEventAdapter = new EventAdapter(this);
        createdEvent.setLayoutManager(createdEventLayoutManager);
        createdEvent.setAdapter(createdEventAdapter);
        joinedEvent.setLayoutManager(joinedEventLayoutManager);
        joinedEvent.setAdapter(joinedEventAdapter);
        loadCreatedEvent();
        loadJoinedEvent();
        gender.setText(user.getGender());
        try {
            Date birthday = df.parse(user.getBirthday());
            Date now = new Date();
            age.setText(String.valueOf(now.getYear()-birthday.getYear()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        country.setText(user.getHomeCountry());
        String userInteret = "";
        for(int i = 0;i < user.getInterests().size(); i++){
            userInteret += user.getInterests().get(i);
            if(i != user.getInterests().size()-1){
                userInteret += ", ";
            }
        }
        interest.setText(userInteret);
        bio.setText(user.getBio());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(user.getDisplayName());



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Direct Message", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                super.onBackPressed();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadCreatedEvent(){
        Log.d(TAG, "loadCreatedEvent: called");
        db.collection("events")
                .whereEqualTo("privacy", "PUBLIC")
                .whereEqualTo("host", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Log.w(TAG, "Getting documents.");
                        if (e != null) {
                            Log.w(TAG, "Error getting documents.", e);
                            return;
                        }
                        createdEventList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            createdEventList.add(document.toObject(Event.class));
                            Log.d(TAG, document.getId() + " added");
                        }
                        createdEventAdapter.setEventList(createdEventList);
                        createdEventAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadJoinedEvent(){
        Log.d(TAG, "loadJoinedEvent: called");
        db.collection("events")
                .whereEqualTo("privacy", "PUBLIC")
                .whereArrayContains("participants", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Log.w(TAG, "Getting documents.");
                        if (e != null) {
                            Log.w(TAG, "Error getting documents.", e);
                            return;
                        }
                        joinedEventList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            joinedEventList.add(document.toObject(Event.class));
                            Log.d(TAG, document.getId() + " added");
                        }
                        if(joinedEventList.size() == 0){
                            noJoinedEvent.setVisibility(View.VISIBLE);
                        }
                        joinedEventAdapter.setEventList(joinedEventList);
                        joinedEventAdapter.notifyDataSetChanged();
                    }
                });
    }
}
