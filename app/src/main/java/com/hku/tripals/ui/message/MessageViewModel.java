package com.hku.tripals.ui.message;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hku.tripals.model.EventChat;
import com.hku.tripals.model.Request;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MessageViewModel extends ViewModel {

    private static final String TAG = "MessageViewModel";
    private MutableLiveData<List<EventChat>> chats;

    private List<EventChat> eventChatList;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public MessageViewModel() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        eventChatList = new ArrayList<>();
        chats = new MutableLiveData<>();
    }

    public LiveData<List<EventChat>> getEventChat(){
        return chats;
    }

    public void loadEventChat(){
        Log.d(TAG, "loadRequest: called");
        db.collection("chats")
                .whereArrayContains("participants", currentUser.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Log.w(TAG, "Getting documents.");
                        if (e != null) {
                            Log.w(TAG, "Error getting documents.", e);
                            return;
                        }
                        eventChatList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            eventChatList.add(document.toObject(EventChat.class));
                            Log.d(TAG, document.getId() + " added");
                        }
                        chats.setValue(eventChatList);
                    }
                });
    }
}