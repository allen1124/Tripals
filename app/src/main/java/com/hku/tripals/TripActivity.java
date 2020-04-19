package com.hku.tripals;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hku.tripals.adapter.SelectableEventAdapter;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Trip;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TripActivity extends AppCompatActivity {

    private static final String TAG = "CreateTripActivity";

    private ImageButton back;
    private ImageView tripPhoto;
    private TextView tripTitle;
    private TextView tripDestination;
    private RecyclerView eventRecyclerView;
    private LinearLayoutManager eventLayoutManager;
    private SelectableEventAdapter selectableEventAdapter;
    private List<Event> eventList = new ArrayList<>();
    private List<Event> selectedList = new ArrayList<>();
    private List<String> eventIdList = new ArrayList<>();

    private Trip trip;
    private Intent intent;
    private int check = 0;

    private FirebaseFirestore db;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    Client searchClient;
    Index index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_view);
        searchClient = new Client(getApplication().getResources().getString(R.string.search_app_id), getApplication().getResources().getString(R.string.search_key));
        index = searchClient.getIndex("events");
        db = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        trip = new Trip();
        back = findViewById(R.id.create_trip_back_imageButton);
        tripPhoto = findViewById(R.id.trip_header_imageView);
        tripTitle = findViewById(R.id.trip_name_editText);
        tripDestination = findViewById(R.id.trip_destination_editText);
        eventRecyclerView = findViewById(R.id.create_trip_events_RecyclerView);
        eventLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        intent = getIntent();
        trip = (Trip) intent.getSerializableExtra("trip");
        Picasso.get().load(trip.getPhotoUrl()).into(tripPhoto);
        tripTitle.setText(trip.getTitle());
        tripDestination.setText(trip.getDestination());

        selectableEventAdapter = new SelectableEventAdapter(this);
        eventRecyclerView.setLayoutManager(eventLayoutManager);
        eventRecyclerView.setAdapter(selectableEventAdapter);

        getEvents();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        });
        selectableEventAdapter.notifyDataSetChanged();
    }

    private void getEvents(){
        db.collection("trips").document(trip.getId()).collection("events").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    eventIdList.add(documentSnapshot.getId());
                    Log.d(TAG, "ID added: " + documentSnapshot.getId());
                    loadEvent();
                }

            }
        });

    }

    private void loadEvent(){
        Log.d(TAG, "loadEvent: called");
        String filter = "";
        filter += "participants:" + currentUser.getUid() + " OR ";
        filter += "host:" + currentUser.getUid();
        Log.d(TAG, "loadEvent: filter: "+filter);
        Query q = new Query().setFilters(filter);
        index.searchAsync(q, new CompletionHandler() {
            @Override
            public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                try{
                    JSONArray hits = jsonObject.getJSONArray("hits");
                    Log.d(TAG, "requestCompleted: "+hits.toString());
                    eventList.clear();
                    for(int i = 0; i < hits.length(); i++){
                        JSONObject eventJSON = hits.getJSONObject(i);
                        Event event = parseJSON(eventJSON);
                        if(event != null) {
                            Log.d(TAG, "Selected events list size: "+ eventIdList.size());
                            for (int j = 0; j < eventIdList.size(); j++){
                                if(event.getId().matches(eventIdList.get(j))){
                                    eventList.add(event);
                                    Log.d(TAG, " eventList.add: "+event.getId());
                                    Log.d(TAG, " eventList.size: "+eventList.size());
                                }
                            }
                        }
                    }
                    Collections.sort(eventList, new Comparator<Event>() {
                        public int compare(Event o1, Event o2) {
                            return o1.getDatetime().compareTo(o2.getDatetime());
                        }
                    });
                    selectableEventAdapter.setEventList(eventList);
                    selectableEventAdapter.notifyDataSetChanged();
                }catch (JSONException err){
                    Log.d(TAG, "requestCompleted: "+err.getMessage());
                    err.printStackTrace();
                }
            }
        });
    }


    private void addTrip(){
        Log.d(TAG, "addEvent: called");
        final DocumentReference TripRef = db.collection("trips").document();
        String newTripId = TripRef.getId();
        trip.setId(newTripId);
        if(selectedList.size() > 1) {
            trip.setPhotoUrl(selectedList.get(0).getPhotoUrl());
        }
        TripRef.set(trip.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot added with ID: " + trip.getId());
                Toast.makeText(TripActivity.this,
                        R.string.create_trip_complete_message, Toast.LENGTH_SHORT).show();
                clearForm();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document", e);
            }
        });
        for(Event selected: selectedList){
            DocumentReference newTripEventRef = db.collection("trips").document(newTripId).collection("events").document(selected.getId());
            newTripEventRef.set(selected.toMap());
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    private void clearForm(){
        tripTitle.setEnabled(true);
        tripDestination.setEnabled(true);
        tripPhoto.setImageResource(R.color.colorPrimary);
        for(Event event: eventList){
            event.setSelected(false);
        }
        selectableEventAdapter.setEventList(eventList);
        selectableEventAdapter.notifyDataSetChanged();
    }

    private Event parseJSON(JSONObject json){
        Event event = null;
        Log.d(TAG, "parseJSON: "+json);
        try{
            List<String> interests = new ArrayList<String>();
            List<String> participants = new ArrayList<String>();
            if(json.has("interests") && !json.isNull("interests")) {
                for (int i = 0; i < json.getJSONArray("interests").length(); i++) {
                    interests.add(json.getJSONArray("interests").getString(i));
                }
            }
            if(json.has("participants") && !json.isNull("participants")) {
                for (int i = 0; i < json.getJSONArray("participants").length(); i++) {
                    participants.add(json.getJSONArray("participants").getString(i));
                }
            }
            event = new Event(
                    json.getString("id"),
                    json.getString("host"),
                    json.getString("hostName"),
                    json.getString("hostAvatarUrl"),
                    participants,
                    json.getString("title"),
                    json.getString("description"),
                    json.getString("privacy"),
                    json.getString("openness"),
                    json.getInt("quota"),
                    new Date(json.getJSONObject("datetime").getLong("_seconds")*1000+ TimeUnit.NANOSECONDS.toMillis(json.getJSONObject("datetime").getInt("_nanoseconds"))),
                    json.getString("location"),
                    json.getString("locationName"),
                    json.getString("photoUrl"),
                    interests,
                    new Date(json.getJSONObject("timestamp").getLong("_seconds")*1000+ TimeUnit.NANOSECONDS.toMillis(json.getJSONObject("timestamp").getInt("_nanoseconds")))
            );
        }catch (JSONException err){
            err.printStackTrace();
        }
        return event;
    }
}
