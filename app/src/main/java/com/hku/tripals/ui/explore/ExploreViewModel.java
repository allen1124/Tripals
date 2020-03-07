package com.hku.tripals.ui.explore;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hku.tripals.R;
import com.hku.tripals.model.Destination;
import com.hku.tripals.model.Event;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExploreViewModel extends ViewModel {

    private static final String TAG = "ExploreViewModel";
    private MutableLiveData<List<Destination>> destinations;
    private MutableLiveData<List<Event>> events;

    private List<Event> eventList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public ExploreViewModel() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        eventList = new ArrayList<>();
        destinations = new MutableLiveData<>();
        events = new MutableLiveData<>();
        List<Destination> destinationsList = new ArrayList<>();
        destinationsList.add(new Destination(1, R.drawable.hong_kong, R.string.hong_kong, "22.396427", "114.109497"));
        destinationsList.add(new Destination(2, R.drawable.macau, R.string.macau, "22.198746", "113.543877"));
        destinationsList.add(new Destination(3, R.drawable.tai_pei, R.string.tai_pei, "25.032969", "121.565414"));
        destinationsList.add(new Destination(4, R.drawable.tokyo, R.string.tokyo, "35.680923", "139.760562"));
        destinationsList.add(new Destination(5, R.drawable.singapore, R.string.singapore, "1.352083", "103.819839"));
        destinationsList.add(new Destination(6, R.drawable.theme_colour, R.string.more, "", ""));
        destinations.setValue(destinationsList);
    }

    public LiveData<List<Destination>> getDestinations() {
        return destinations;
    }

    public LiveData<List<Event>> getEvents(){
        return events;
    }

    public void loadEvent(int numberEvent){
        Log.d(TAG, "loadEvent: called");
        db.collection("events")
                .limit(numberEvent)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        eventList.add(document.toObject(Event.class));
                        Log.d(TAG, document.getId() + " added");
                    }
                    events.setValue(eventList);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }
}