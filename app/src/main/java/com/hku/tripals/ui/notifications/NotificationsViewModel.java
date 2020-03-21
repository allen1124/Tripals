package com.hku.tripals.ui.notifications;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Request;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class NotificationsViewModel extends ViewModel {

    private static final String TAG = "NotificationsViewModel";

    private MutableLiveData<List<Request>> requests;

    private List<Request> requestList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public NotificationsViewModel() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        requestList = new ArrayList<>();
        requests = new MutableLiveData<>();
    }

    public LiveData<List<Request>> getRequest(){
        return requests;
    }

    public void loadRequest(){
        Log.d(TAG, "loadRequest: called");
        db.collection("requests")
                .whereEqualTo("hostUid", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Log.w(TAG, "Getting documents.");
                        if (e != null) {
                            Log.w(TAG, "Error getting documents.", e);
                            return;
                        }
                        requestList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            requestList.add(document.toObject(Request.class));
                            Log.d(TAG, document.getId() + " added");
                        }
                        requests.setValue(requestList);
                    }
                });
    }
}