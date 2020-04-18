package com.hku.tripals.ui.favourite;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hku.tripals.R;
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

import static android.content.Context.MODE_PRIVATE;

public class FavouriteViewModel extends AndroidViewModel {

    private static final String TAG = "FavouriteViewModel";
    private static final String BOOKMARK_PREF = "BOOKMARK_PREF";

    private MutableLiveData<List<Event>> events;
    private MutableLiveData<List<User>> users;

    private List<Event> eventList;
    private List<User> userList;

    SharedPreferences bookmarkPref, userbookmarkPref;
    private List<String> bookmarkList = new ArrayList<>();
    private String bookmarkJson;

    private List<String> userbookmarkList = new ArrayList<>();
    private String userbookmarkJson;

    private FirebaseFirestore db;
    Client searchClient;
    Index index;

    public FavouriteViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        searchClient = new Client(application.getResources().getString(R.string.search_app_id), application.getResources().getString(R.string.search_key));
        index = searchClient.getIndex("events");
        eventList = new ArrayList<>();
        events = new MutableLiveData<>();
        userList = new ArrayList<>();
        users = new MutableLiveData<>();
    }

    public void loadEvent(){
        bookmarkPref = getApplication().getSharedPreferences(BOOKMARK_PREF, MODE_PRIVATE);
        bookmarkJson = bookmarkPref.getString("bookmark", "[]");
        JSONArray jsonArray = null;
        bookmarkList.clear();
        try {
            jsonArray = new JSONArray(bookmarkJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                bookmarkList.add(jsonArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "loadEvent: "+bookmarkList.toString());
        if(bookmarkList.size() > 0) {
            String filter = "";
            for(int i = 0; i < bookmarkList.size(); i++){
                if(i != bookmarkList.size()-1) {
                    filter += "id:" + bookmarkList.get(i) + " OR ";
                }else{
                    filter += "id:" + bookmarkList.get(i);
                }
            }
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
                            if(event != null)
                                eventList.add(event);
                        }
                        events.setValue(eventList);
                    }catch (JSONException err){
                        Log.d(TAG, "requestCompleted: "+err.getMessage());
                        err.printStackTrace();
                    }
                }
            });
        }else {
            eventList.clear();
            events.setValue(eventList);
        }
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

    public void loadUser(){
        userbookmarkPref = getApplication().getSharedPreferences(BOOKMARK_PREF, MODE_PRIVATE);
        userbookmarkJson = userbookmarkPref.getString("bookmark", "[]");
        JSONArray jsonArray = null;
        userbookmarkList.clear();
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
        Log.d(TAG, "loadUser: "+userbookmarkList.toString());
        if(userbookmarkList.size() > 0) {
            db.collection("user-profile")
                    .whereIn(FieldPath.documentId(), userbookmarkList)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            Log.w(TAG, "Getting documents.");
                            if (e != null) {
                                Log.w(TAG, "Error getting documents.", e);
                                return;
                            }
                            userList.clear();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                userList.add(document.toObject(User.class));
                                Log.d(TAG, document.getId() + " added");
                            }
                            users.setValue(userList);
                        }
                    });
        }else {
            userList.clear();
            users.setValue(userList);
        }
    }

    public LiveData<List<Event>> getEvents(){
        return events;
    }

    public LiveData<List<User>> getUsers(){
        return users;
    }
}