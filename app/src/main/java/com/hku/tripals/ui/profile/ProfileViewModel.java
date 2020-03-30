package com.hku.tripals.ui.profile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hku.tripals.model.User;

public class ProfileViewModel extends ViewModel {

    private static final String TAG = "ProfileViewModel";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private MutableLiveData<User> mUser;

    public ProfileViewModel() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mUser = new MutableLiveData<>();

    }

    public LiveData<User> getUser() {
        //User user = new User();
        //user.setAvatarImageUrl(currentUser.getPhotoUrl().toString());
        //user.setDisplayName(currentUser.getDisplayName());
        //mUser.setValue(user);
        loadProfile();
        return mUser;
    }

    private void loadProfile(){
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
}