package com.hku.tripals.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hku.tripals.LoginActivity;
import com.hku.tripals.R;
import com.hku.tripals.StartActivity;
import com.hku.tripals.model.User;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private ProfileViewModel profileViewModel;
    private ImageView avatar;
    private TextView username;
    private TextView gender;
    private TextView homeCountry;
    private TextView interests;
    private TextView bio;
    private Button logout;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        avatar = root.findViewById(R.id.profile_user_avatar_imageView);
        username = root.findViewById(R.id.profile_user_display_name_textView);
        gender = root.findViewById(R.id.profile_user_gender_textView);
        homeCountry = root.findViewById(R.id.profile_user_home_country_textView);
        interests = root.findViewById(R.id.profile_user_interests_textView);
        bio = root.findViewById(R.id.profile_user_bio_textView);
        logout = root.findViewById(R.id.profile_user_logout_button);

        profileViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                //profileViewModel.loadProfile();
                username.setText(user.getDisplayName());
                gender.setText(user.getGender());
                homeCountry.setText(user.getHomeCountry());
                bio.setText(user.getBio());
                String interestList = user.getInterests().toString();
                String interestString = interestList.substring(1, interestList.length() - 1);
                interests.setText(interestString);
                Glide.with(getActivity())
                        .load(user.getAvatarImageUrl())
                        .circleCrop()
                        .into(avatar);
            }
        });

        setupFirebaseAuth();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                getActivity().finish();
                Intent intent = new Intent(getActivity(), StartActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });



        return root;
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");

                    Log.d(TAG, "onAuthStateChanged: navigating back to login screen.");
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                // ...
            }
        };
    }


}
