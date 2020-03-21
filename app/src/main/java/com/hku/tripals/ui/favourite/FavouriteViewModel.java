package com.hku.tripals.ui.favourite;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hku.tripals.model.Event;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

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

    private List<Event> eventList;

    SharedPreferences bookmarkPref;
    private List<String> bookmarkList = new ArrayList<>();
    private String bookmarkJson;

    private FirebaseFirestore db;

    public FavouriteViewModel(@NonNull Application application) {
        super(application);
        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();
        events = new MutableLiveData<>();
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
            db.collection("events")
                    .whereIn(FieldPath.documentId(), bookmarkList)
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
        }else {
            eventList.clear();
            events.setValue(eventList);
        }
    }

    public LiveData<List<Event>> getEvents(){
        return events;
    }
}