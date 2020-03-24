package com.hku.tripals.ui.profile;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
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
    private DocumentReference docRef;

    private User user;
    private MutableLiveData<User> mUser;

    public ProfileViewModel() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mUser = new MutableLiveData<>();
    }

    public LiveData<User> getUser() {
        user = new User();
        docRef = db.collection("user-profile").document(currentUser.getUid().toString());
        Task<DocumentSnapshot> task = docRef.get();
        DocumentSnapshot document = task.getResult();
        if (document.exists()) {
            Log.d(TAG, "DocumentSnapshot data: " + document.getData()); //Success
            Log.d(TAG, "User gender 1: " + document.get("gender").toString());
            user.setGender(document.get("gender").toString());
            user.setBio(document.get("bio").toString());
            user.setHomeCountry(document.get("homeCountry").toString());
            user.setBirthday(document.get("birthday").toString());
            user.setLanguage(document.get("language").toString());
            //user.setInterests(document.get("interests").getClass(List <string>)); //current testing
            Log.d(TAG, "User: " + user.getGender() + user.getBio() + user.getHomeCountry());
        } else {
            Log.d(TAG, "No such document");
        }
        user.setAvatarImageUrl(currentUser.getPhotoUrl().toString());
        user.setDisplayName(currentUser.getDisplayName());
        mUser.setValue(user);
        return mUser;
    }
}