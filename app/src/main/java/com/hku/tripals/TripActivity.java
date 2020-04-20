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
import com.hku.tripals.adapter.EventAdapter;
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

    private static final String TAG = "TripActivity";

    private ImageButton back;
    private ImageView tripPhoto;
    private TextView tripTitle;
    private TextView tripDestination;
    private RecyclerView eventRecyclerView;
    private LinearLayoutManager eventLayoutManager;
    private EventAdapter eventAdapter;
    private List<Event> eventList = new ArrayList<>();

    private Trip trip;
    private Intent intent;

    private FirebaseFirestore db;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_view);
        db = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        trip = new Trip();
        back = findViewById(R.id.create_trip_back_imageButton);
        tripPhoto = findViewById(R.id.trip_header_imageView);
        tripTitle = findViewById(R.id.trip_title_textView);
        tripDestination = findViewById(R.id.trip_destination_textView);
        eventRecyclerView = findViewById(R.id.create_trip_events_RecyclerView);
        eventLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        intent = getIntent();
        trip = (Trip) intent.getSerializableExtra("trip");
        Picasso.get().load(trip.getPhotoUrl()).into(tripPhoto);
        tripTitle.setText(trip.getTitle());
        tripDestination.setText(trip.getDestination());

        eventAdapter = new EventAdapter(this);
        eventRecyclerView.setLayoutManager(eventLayoutManager);
        eventRecyclerView.setAdapter(eventAdapter);

        loadEvent();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        });
    }

    private void loadEvent(){
        Log.d(TAG, "loadJoinedEvent: called");
        db.collection("trips").document(trip.getId()).collection("events")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Log.w(TAG, "Getting documents.");
                        if (e != null) {
                            Log.w(TAG, "Error getting documents.", e);
                            return;
                        }
                        eventList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            eventList.add(document.toObject(Event.class));
                            Log.d(TAG, document.getId() + " added");
                        }
                        eventAdapter.setEventList(eventList);
                        eventAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
