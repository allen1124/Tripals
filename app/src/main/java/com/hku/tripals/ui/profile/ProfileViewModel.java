package com.hku.tripals.ui.profile;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
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

    private User user = new User();

    private MutableLiveData<User> mUser;

    public ProfileViewModel() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mUser = new MutableLiveData<>();
        docRef = db.collection("user-profile").document(currentUser.getUid().toString());
    }

    public LiveData<User> getUser() {

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData()); //Success
                        user.setgender(document.get("gender").toString());
                        user.sethomeCountry(document.get("homeCountry").toString());
                        user.setbirthday(document.get("birthday").toString());
                        user.setlanguage(document.get("language").toString());
                        user.setbio(document.get("bio").toString());
                        user.setInterests(document.get("interests").toObject); //current testing
                        Log.d(TAG, "User: " + user.getUser()); //cannot show
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        user.setAvatarImageUrl(currentUser.getPhotoUrl().toString());
        user.setDisplayName(currentUser.getDisplayName());
        mUser.setValue(user);
        return mUser;
    }
}