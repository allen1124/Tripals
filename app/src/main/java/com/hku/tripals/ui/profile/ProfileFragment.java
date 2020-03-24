package com.hku.tripals.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.hku.tripals.R;
import com.hku.tripals.model.User;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private ProfileViewModel profileViewModel;
    private ImageView avatar;
    private TextView username;
    private TextView gender;
    private TextView homeCountry;
    private TextView bio;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel =
                ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        avatar = root.findViewById(R.id.profile_user_avatar_imageView);
        username = root.findViewById(R.id.profile_user_display_name_textView);
        homeCountry = root.findViewById(R.id.profile_user_home_country_textView);
        gender = root.findViewById(R.id.profile_user_gender_textView);
        bio = root.findViewById(R.id.profile_user_bio_textView);

        profileViewModel.getUser().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                username.setText(user.getDisplayName());

                gender.setText(user.getGender());
                homeCountry.setText(user.getHomeCountry());
                bio.setText(user.getBio());

                Glide.with(getActivity())
                        .load(user.getAvatarImageUrl())
                        .circleCrop()
                        .into(avatar);

                Log.d(TAG, "Get User");
            }
        });

        return root;
    }
}