package com.hku.tripals.ui.profile;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hku.tripals.model.User;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileViewModel extends ViewModel {

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
        User user = new User();
        user.setAvatarImageUrl(currentUser.getPhotoUrl().toString());
        user.setDisplayName(currentUser.getDisplayName());
        mUser.setValue(user);
        return mUser;
    }
}