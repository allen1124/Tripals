package com.hku.tripals.ui.explore;

import android.app.Application;
import android.util.Log;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hku.tripals.R;
import com.hku.tripals.model.Destination;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ExploreViewModel extends AndroidViewModel {

    private static final String TAG = "ExploreViewModel";
    private MutableLiveData<List<Destination>> destinations;
    private MutableLiveData<List<Event>> events;
    private MutableLiveData<List<Event>> YMIevents;

    private List<Event> eventList;
    private List<Event> ymiEventList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    Client searchClient;
    Index index;

    public ExploreViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        eventList = new ArrayList<>();
        destinations = new MutableLiveData<>();
        events = new MutableLiveData<>();
        YMIevents = new MutableLiveData<>();
        ymiEventList = new ArrayList<>();
        searchClient = new Client(getApplication().getResources().getString(R.string.search_app_id), getApplication().getResources().getString(R.string.search_key));
        index = searchClient.getIndex("events");
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
                .whereEqualTo("privacy", "PUBLIC")
                .limit(numberEvent)
                .orderBy("timestamp", Query.Direction.DESCENDING)
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
                        events.setValue(eventList);
                    }
                });
    }

    public LiveData<List<Event>> getYMIEvents(){
        return YMIevents;
    }

    public void loadYMIEvent(int numberEvent){
        db.collection("user-profile").document(currentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                List<String> interest = user.getInterests();
                String filter = "";
                for(int i = 0; i < interest.size(); i++){
                    if(i != interest.size()-1) {
                        filter += "interests:" + interest.get(i) + " AND ";
                    }else{
                        filter += "interests:" + interest.get(i);
                    }
                }
                filter += " AND privacy:PUBLIC";
                Log.d(TAG, "filter:"+filter);
                com.algolia.search.saas.Query q = new com.algolia.search.saas.Query().setFilters(filter);
                searchEvent(q);
            }
        });
    }

    private void searchEvent(com.algolia.search.saas.Query query){
        Log.d(TAG, "searchEvent: Query"+query.getQuery());
        Log.d(TAG, "searchEvent: Filter"+query.getFilters());
        index.searchAsync(query, new CompletionHandler() {
            @Override
            public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                try{
                    JSONArray hits = jsonObject.getJSONArray("hits");
                    ymiEventList = new ArrayList<>();
                    for(int i = 0; i < hits.length(); i++){
                        JSONObject eventJSON = hits.getJSONObject(i);
                        Event event = parseJSON(eventJSON);
                        if(event != null)
                            ymiEventList.add(event);
                    }
                    YMIevents.setValue(ymiEventList);
                }catch (JSONException err){
                    err.printStackTrace();
                }
            }
        });
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