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

import com.hku.tripals.R;
import com.hku.tripals.adapter.EventAdapter;
import com.hku.tripals.adapter.RequestAdapter;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Request;

import java.util.List;

public class FavouriteFragment extends Fragment {

    private static final String TAG = "FavouriteFragment";
    private FavouriteViewModel favouriteViewModel;

    private RecyclerView eventRecyclerView;
    private TextView noFavourite;
    private LinearLayoutManager favouriteLayoutManager;
    private EventAdapter eventAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        favouriteViewModel =
                ViewModelProviders.of(this).get(FavouriteViewModel.class);
        View root = inflater.inflate(R.layout.fragment_favourite, container, false);
        eventRecyclerView = root.findViewById(R.id.favourite_event_RecyclerView);
        noFavourite = root.findViewById(R.id.no_favourite_textView);
        favouriteLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        eventRecyclerView.setLayoutManager(favouriteLayoutManager);
        eventAdapter = new EventAdapter(getActivity());
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
        favouriteViewModel.loadEvent();
        eventRecyclerView.setAdapter(eventAdapter);
        return root;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: called");
        super.onResume();
        favouriteViewModel.loadEvent();
    }
}