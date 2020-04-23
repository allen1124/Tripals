package com.hku.tripals;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hku.tripals.adapter.EventAdapter;
import com.hku.tripals.adapter.TripAdapter;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Trip;
import com.hku.tripals.model.User;
import com.hku.tripals.ui.message.MessageActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";
    private static final String USER_BOOKMARK_PREF = "USER_BOOKMARK_PREF";
    //can test more than 10 values by using the PREF below instead of the one above
    //private static final String USER_BOOKMARK_PREF = "BOOKMARK_PREF";
    private User user;

    //variables for individual chat
    private String currentUserName;
    private String currentUserUrl;
    private String TargetID;
    private String firmedName;
    private String firmedUrl;
    private int firmedChatID = 2;

    private ImageView avatar;
    private TextView gender;
    private TextView age;
    private TextView country;
    private TextView interest;
    private TextView bio;
    private TextView FB_btn;
    private String FB_url;
    private TextView IG_btn;
    private String IG_url;
    private TextView eventCreatedTitle;
    private TextView noJoinedEvent;
    private RecyclerView createdEvent;
    private RecyclerView joinedEvent;
    private LinearLayoutManager createdEventLayoutManager;
    private LinearLayoutManager joinedEventLayoutManager;
    private EventAdapter createdEventAdapter, joinedEventAdapter;
    private List<Event> createdEventList = new ArrayList<>();
    private List<Event> joinedEventList = new ArrayList<>();
    private List<Trip> createdTripList = new ArrayList<>();

    private CircleImageView bookmarkButton;
    private boolean bookmarked = false;
    SharedPreferences userbookmarkPref;
    private List<String> userbookmarkList = new ArrayList<>();
    private String userbookmarkJson;

    private ConstraintLayout constraintLayout;
    private TabLayout createdTab;
    private RecyclerView createdTripsRecyclerView;
    private LinearLayoutManager createdTripsLayoutManager;
    private TripAdapter createdTripAdapter;

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
        FB_btn = findViewById(R.id.profile_Facebook_textview);
        IG_btn = findViewById(R.id.profile_Instagram_textview);
        constraintLayout = findViewById(R.id.user_profile_constraintLayout);
        noJoinedEvent = findViewById(R.id.no_joined_event_textView);
        noJoinedEvent.setVisibility(View.GONE);
        createdEvent = findViewById(R.id.user_created_event_RecyclerView);
        joinedEvent = findViewById(R.id.user_joined_event_RecyclerView);
        createdTab = findViewById(R.id.user_created_tabLayout);
        createdTripsRecyclerView = findViewById(R.id.user_created_trip_RecyclerView);
        createdTripsLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        createdEventLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        joinedEventLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        createdTripAdapter = new TripAdapter(this);
        createdEventAdapter = new EventAdapter(this);
        joinedEventAdapter = new EventAdapter(this);
        createdEvent.setLayoutManager(createdEventLayoutManager);
        createdEvent.setAdapter(createdEventAdapter);
        joinedEvent.setLayoutManager(joinedEventLayoutManager);
        joinedEvent.setAdapter(joinedEventAdapter);
        createdTripsRecyclerView.setLayoutManager(createdTripsLayoutManager);
        createdTripsRecyclerView.setAdapter(createdTripAdapter);
        loadCreatedEvent();
        loadJoinedEvent();
        loadCreatedTrip();

        bookmarkButton = findViewById(R.id.user_bookmark);
        userbookmarkPref = getSharedPreferences(USER_BOOKMARK_PREF, MODE_PRIVATE);
        userbookmarkJson = userbookmarkPref.getString("bookmark", "[]");
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(userbookmarkJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                userbookmarkList.add(jsonArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        bookmarked = userbookmarkList.contains(user.getUid());
        if(bookmarked){
            bookmarkButton.setImageResource(R.mipmap.ic_bookmark_on);
        }else{
            bookmarkButton.setImageResource(R.mipmap.ic_bookmark_off);
        }
        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = userbookmarkPref.edit();
                if(bookmarked){
                    bookmarkButton.setImageResource(R.mipmap.ic_bookmark_off);
                    bookmarked = false;
                    userbookmarkList.remove(user.getUid());
                }else{
                    bookmarkButton.setImageResource(R.mipmap.ic_bookmark_on);
                    bookmarked = true;
                    userbookmarkList.add(user.getUid());
                }
                JSONArray json = new JSONArray(userbookmarkList);
                editor.putString("bookmark", json.toString());
                Log.d(TAG, "user bookmark in json: "+ json.toString());
                editor.commit();
            }
        });

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

        DocumentReference docRef = db.collection("user-profile").document(currentUser.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                currentUserName = documentSnapshot.get("displayName").toString();
                currentUserUrl = documentSnapshot.get("avatarImageUrl").toString();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Please click again", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                addChatRoom(user);
            }
        });

        //Facebook Button
        if (user.getFacebook().isEmpty()){
            FB_btn.setVisibility(View.GONE);
            FB_url = "No Facebook";
        } else {
            FB_url = user.getFacebook();
        }

        FB_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fb_link = "https://fb.com/" + FB_url;
                Log.d(TAG, fb_link);
                clicked_btn(fb_link);
            }
        });

        //Instagram Button
        if (user.getInstagram().isEmpty()){
            IG_btn.setVisibility(View.GONE);
            IG_url = "No Instagram";
        } else {
            IG_url = user.getInstagram();
        }

        IG_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ig_link = "https://instagram.com/" + IG_url;
                Log.d(TAG, ig_link);
                clicked_btn(ig_link);
            }
        });


        createdTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "tab selected" +tab.getPosition());
                if(tab.getPosition() == 0) {
                    createdTripsRecyclerView.setVisibility(View.GONE);
                    createdEvent.setVisibility(View.VISIBLE);
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(constraintLayout);
                    constraintSet.connect(R.id.user_joined_event_title_textView, ConstraintSet.TOP, R.id.user_created_event_RecyclerView,ConstraintSet.BOTTOM,0);
                    constraintSet.applyTo(constraintLayout);
                }
                if(tab.getPosition() == 1) {
                    createdEvent.setVisibility(View.GONE);
                    createdTripsRecyclerView.setVisibility(View.VISIBLE);
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(constraintLayout);
                    constraintSet.connect(R.id.user_joined_event_title_textView, ConstraintSet.TOP, R.id.user_created_trip_RecyclerView,ConstraintSet.BOTTOM,0);
                    constraintSet.applyTo(constraintLayout);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
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

    public void loadCreatedTrip(){
        Log.d(TAG, "loadJoinedEvent: called");
        db.collection("trips")
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
                        createdTripList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            createdTripList.add(document.toObject(Trip.class));
                            Log.d(TAG, document.getId() + " added");
                        }
                        createdTripAdapter.setTripList(createdTripList);
                        createdTripAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void addChatRoom(User user){
        final String chatID = currentUser.getUid() + user.getUid();
        final String chatID2 = user.getUid() + currentUser.getUid();
        TargetID = user.getUid();
        firmedName = user.getDisplayName();
        firmedUrl = user.getAvatarImageUrl();

        DocumentReference dbRef = db.collection("chats").document(chatID);
        final DocumentReference dbRef2 = db.collection("chats").document(chatID2);

        dbRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    firmedChatID = 0;
                    gotoMsg(firmedChatID);
                    firmedChatID = 2;
                } else {
                    dbRef2.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()){
                                firmedChatID = 1;
                                gotoMsg(firmedChatID);
                                firmedChatID = 2;
                            }
                            else {
                                List<String> participant = new ArrayList<>();
                                List<String> participantName = new ArrayList<>();
                                List<String> participantUrl = new ArrayList<>();
                                participant.add(currentUser.getUid());
                                participant.add(TargetID);
                                participantName.add(currentUserName);
                                participantUrl.add(currentUserUrl);
                                participantName.add(firmedName);
                                participantUrl.add(firmedUrl);

                                firmedChatID = 0;
                                HashMap<String, Object> chats = new HashMap<>();
                                chats.put("eventId", chatID);
                                chats.put("participants", participant);
                                chats.put("participantName", participantName);
                                chats.put("participantPhotoUrl", participantUrl);
                                chats.put("type", "INDIVIDUAL");
                                db.collection("chats").document(chatID).set(chats);

                                gotoMsg(firmedChatID);
                                firmedChatID = 2;
                            }
                        }
                    });
                }
            }
        });
    }

    private void gotoMsg(int firmedChatID){
        Intent chatIntent = new Intent(UserProfileActivity.this, MessageActivity.class);
        if (firmedChatID == 0){
            chatIntent.putExtra("Chat_Id", currentUser.getUid() + TargetID);
        } else if (firmedChatID == 1){
            chatIntent.putExtra("Chat_Id", TargetID + currentUser.getUid());
        }
        chatIntent.putExtra("Chat_Name", firmedName);
        chatIntent.putExtra("Chat_Icon", firmedUrl);
        chatIntent.putExtra("type", "INDIVIDUAL");
        chatIntent.putExtra("participants", TargetID);
        startActivity(chatIntent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    //Open Browser HTTP
    public void clicked_btn(String url) {
        Intent intent = new Intent (Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
