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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hku.tripals.LoginActivity;
import com.hku.tripals.R;
import com.hku.tripals.StartActivity;
import com.hku.tripals.adapter.EventAdapter;
import com.hku.tripals.adapter.TripAdapter;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Trip;
import com.hku.tripals.model.User;

import java.util.List;

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

    private RecyclerView createdEventRecyclerView;
    private LinearLayoutManager createdEventLayoutManager;
    private EventAdapter createdEventAdapter;

    private RecyclerView joinedEventRecyclerView;
    private LinearLayoutManager joinedEventLayoutManager;
    private EventAdapter joinedEventAdapter;

    private ConstraintLayout constraintLayout;
    private TabLayout createdTab;
    private RecyclerView createdTripsRecyclerView;
    private LinearLayoutManager createdTripsLayoutManager;
    private TripAdapter createdTripAdapter;

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
        createdTab = root.findViewById(R.id.profile_created_tabLayout);
        constraintLayout = root.findViewById(R.id.profile_user_constraintLayout);
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

        //Created Event
        createdEventRecyclerView  = root.findViewById(R.id.created_events_RecyclerView);
        createdEventLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        createdEventRecyclerView.setLayoutManager(createdEventLayoutManager);
        createdEventAdapter = new EventAdapter(getActivity());
        profileViewModel.getCreatedEvents().observe(getViewLifecycleOwner(), new Observer<List<Event>>() {
            @Override
            public void onChanged(List<Event> events) {
                createdEventAdapter.setEventList(events);
                createdEventAdapter.notifyDataSetChanged();
            }
        });
        profileViewModel.loadCreatedEvent();
        createdEventRecyclerView.setAdapter(createdEventAdapter);
        Log.d(TAG, "createdEventAdapter: "+createdEventAdapter);

        //Joined Event
        joinedEventRecyclerView  = root.findViewById(R.id.joined_events_RecyclerView);
        joinedEventLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        joinedEventRecyclerView.setLayoutManager(joinedEventLayoutManager);
        joinedEventAdapter = new EventAdapter(getActivity());
        profileViewModel.getJoinedEvents().observe(getViewLifecycleOwner(), new Observer<List<Event>>() {
            @Override
            public void onChanged(List<Event> events) {
                joinedEventAdapter.setEventList(events);
                joinedEventAdapter.notifyDataSetChanged();
            }
        });
        profileViewModel.loadJoinedEvent();
        profileViewModel.loadCreatedEvent();
        profileViewModel.loadCreatedTrip();
        joinedEventRecyclerView.setAdapter(joinedEventAdapter);
        Log.d(TAG, "createdEventAdapter: "+joinedEventAdapter);


        createdTripsRecyclerView = root.findViewById(R.id.created_trips_RecyclerView);
        createdTripsLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        createdTripsRecyclerView.setLayoutManager(createdTripsLayoutManager);
        createdTripAdapter = new TripAdapter(getActivity());
        profileViewModel.getCreatedTrip().observe(getViewLifecycleOwner(), new Observer<List<Trip>>() {
            @Override
            public void onChanged(List<Trip> trips) {
                createdTripAdapter.setTripList(trips);
                joinedEventAdapter.notifyDataSetChanged();
            }
        });
        createdTripsRecyclerView.setAdapter(createdTripAdapter);

        //Logout Button
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

        createdTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "tab selected" +tab.getPosition());
                if(tab.getPosition() == 0) {
                    createdTripsRecyclerView.setVisibility(View.GONE);
                    createdEventRecyclerView.setVisibility(View.VISIBLE);
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(constraintLayout);
                    constraintSet.connect(R.id.joined_events_textView, ConstraintSet.TOP, R.id.created_events_RecyclerView,ConstraintSet.BOTTOM,0);
                    constraintSet.applyTo(constraintLayout);
                }
                if(tab.getPosition() == 1) {
                    createdEventRecyclerView.setVisibility(View.GONE);
                    createdTripsRecyclerView.setVisibility(View.VISIBLE);
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(constraintLayout);
                    constraintSet.connect(R.id.joined_events_textView, ConstraintSet.TOP, R.id.created_trips_RecyclerView,ConstraintSet.BOTTOM,0);
                    constraintSet.applyTo(constraintLayout);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
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
