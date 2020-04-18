package com.hku.tripals.ui.favourite;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.hku.tripals.R;
import com.hku.tripals.adapter.EventAdapter;
import com.hku.tripals.adapter.RequestAdapter;
import com.hku.tripals.adapter.UserAdapter;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Request;
import com.hku.tripals.model.User;

import org.w3c.dom.Text;

import java.util.List;

public class FavouriteFragment extends Fragment {

    private static final String TAG = "FavouriteFragment";
    private FavouriteViewModel favouriteViewModel;

    private RecyclerView eventRecyclerView;
    private TextView noFavourite;
    private LinearLayoutManager favouriteLayoutManager;
    private EventAdapter eventAdapter;

    private RecyclerView userRecyclerView;
    private TextView noFavourite_user;
    private LinearLayoutManager userLayoutManager;
    private UserAdapter userAdapter;

    private TabLayout favouriteTab;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        favouriteViewModel =
                ViewModelProviders.of(this).get(FavouriteViewModel.class);
        View root = inflater.inflate(R.layout.fragment_favourite, container, false);
        favouriteTab = root.findViewById(R.id.tabLayout_favourite);
        eventRecyclerView = root.findViewById(R.id.favourite_event_RecyclerView);
        noFavourite = root.findViewById(R.id.no_favourite_textView);
        favouriteLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        eventRecyclerView.setLayoutManager(favouriteLayoutManager);
        eventAdapter = new EventAdapter(getActivity());

        userRecyclerView = root.findViewById(R.id.favourite_user_RecyclerView);
        noFavourite_user = root.findViewById(R.id.no_favourite_user_textView);
        userLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        userRecyclerView.setLayoutManager(userLayoutManager);
        userAdapter = new UserAdapter(getActivity());


        favouriteViewModel.getEvents().observe(getViewLifecycleOwner(), new Observer<List<Event>>() {
            @Override
            public void onChanged(List<Event> events) {
                Log.d(TAG, "onChanged: "+events.size());
                eventAdapter.setEventList(events);
                eventAdapter.notifyDataSetChanged();
                if(eventAdapter.getItemCount() > 0)
                    noFavourite.setVisibility(View.GONE);
                else
                    noFavourite.setVisibility(View.VISIBLE);
            }
        });
        eventRecyclerView.setAdapter(eventAdapter);

        userRecyclerView.setAdapter(userAdapter);
        favouriteViewModel.getUsers().observe(getViewLifecycleOwner(), new Observer<List<User>>() {
            @Override
            public void onChanged(List<User> users) {
                Log.d(TAG, "onChanged: "+users.size());
                userAdapter.setUserList(users);
                userAdapter.notifyDataSetChanged();
                if(favouriteTab.getSelectedTabPosition() == 1) {
                    if (userAdapter.getItemCount() > 0)
                        noFavourite_user.setVisibility(View.GONE);
                    else
                        noFavourite_user.setVisibility(View.VISIBLE);
                }
            }
        });

        favouriteTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d(TAG, "tab selected" +tab.getPosition());
                if(tab.getPosition() == 0) {
                    noFavourite_user.setVisibility(View.GONE);
                    favouriteViewModel.loadEvent();
                    eventRecyclerView.setVisibility(View.VISIBLE);
                    userRecyclerView.setVisibility(View.GONE);

                }
                if(tab.getPosition() == 1) {
                    eventRecyclerView.setVisibility(View.GONE);
                    noFavourite.setVisibility(View.GONE);
                    favouriteViewModel.loadUser();
                    userRecyclerView.setVisibility(View.VISIBLE);
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

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: called");
        super.onResume();
        favouriteViewModel.loadEvent();
        favouriteViewModel.loadUser();
    }
}