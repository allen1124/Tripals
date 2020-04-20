package com.hku.tripals.ui.profile;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnSuccessListener;
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
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Trip;
import com.hku.tripals.model.User;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewModel extends ViewModel {

    private static final String TAG = "ProfileViewModel";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private MutableLiveData<User> mUser;

    private MutableLiveData<List<Event>> created_events;
    private List<Event> created_eventList;

    private MutableLiveData<List<Event>> joined_events;
    private List<Event> joined_eventList;

    private MutableLiveData<List<Trip>> created_trip;
    private List<Trip> created_tripList;

    public ProfileViewModel() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mUser = new MutableLiveData<>();

        created_eventList = new ArrayList<>();
        created_events = new MutableLiveData<>();

        joined_eventList = new ArrayList<>();
        joined_events = new MutableLiveData<>();

        created_tripList = new ArrayList<>();
        created_trip = new MutableLiveData<>();
    }

    //Users

    public LiveData<User> getUser() {
        //User user = new User();
        //user.setAvatarImageUrl(currentUser.getPhotoUrl().toString());
        //user.setDisplayName(currentUser.getDisplayName());
        //mUser.setValue(user);
        loadProfile();
        return mUser;
    }

    public void loadProfile(){
        Log.d(TAG, "loadProfile: called");
        DocumentReference docRef = db.collection("user-profile").document(currentUser.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                Log.d(TAG, "user.getDisplayName(): " + user.getDisplayName());
                Log.d(TAG, "user.getBio(): " + user.getBio());
                Log.d(TAG, "user.getGender(): " + user.getGender());
                Log.d(TAG, "user.getInterests(): " + user.getInterests().toString());
                Log.d(TAG, "user.getHomeCountry(): " + user.getHomeCountry());
                Log.d(TAG, "user.getLanguage(): " + user.getLanguage());
                mUser.setValue(user);
            }
        });
    }

    //Created Events

    public LiveData<List<Event>> getCreatedEvents(){
        return created_events;
    }

    public void loadCreatedEvent(){
        Log.d(TAG, "loadCreateEvent: called");
        db.collection("events")
                .whereEqualTo("host", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Log.w(TAG, "Getting documents.");
                        if (e != null) {
                            Log.w(TAG, "Error getting documents.", e);
                            return;
                        }
                        created_eventList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            created_eventList.add(document.toObject(Event.class));
                            Log.d(TAG, document.getId() + " added");
                        }
                        created_events.setValue(created_eventList);
                    }
                });
    }

    //Joined Events

    public LiveData<List<Event>> getJoinedEvents(){
        return joined_events;
    }

    public void loadJoinedEvent(){
        Log.d(TAG, "loadJoinedEvent: called");
        db.collection("events")
                .whereEqualTo("privacy", "PUBLIC")
                .whereArrayContains("participants", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Log.w(TAG, "Getting documents.");
                        if (e != null) {
                            Log.w(TAG, "Error getting documents.", e);
                            return;
                        }
                        joined_eventList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            joined_eventList.add(document.toObject(Event.class));
                            Log.d(TAG, document.getId() + " added");
                        }
                        joined_events.setValue(joined_eventList);
                    }
                });
    }

    public LiveData<List<Trip>> getCreatedTrip() {
        return created_trip;
    }

    public void loadCreatedTrip(){
        Log.d(TAG, "loadJoinedEvent: called");
        db.collection("trips")
                .whereEqualTo("host", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Log.w(TAG, "Getting documents.");
                        if (e != null) {
                            Log.w(TAG, "Error getting documents.", e);
                            return;
                        }
                        created_tripList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            created_tripList.add(document.toObject(Trip.class));
                            Log.d(TAG, document.getId() + " added");
                        }
                        created_trip.setValue(created_tripList);
                    }
                });
    }
}