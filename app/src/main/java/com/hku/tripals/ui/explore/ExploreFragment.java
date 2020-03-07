package com.hku.tripals.ui.explore;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hku.tripals.CreateEventActivity;
import com.hku.tripals.MapsActivity;
import com.hku.tripals.PlaceActivity;
import com.hku.tripals.SearchResultsActivity;
import com.hku.tripals.R;
import com.hku.tripals.adapter.DestinationAdapter;
import com.hku.tripals.adapter.EventAdapter;
import com.hku.tripals.model.Destination;
import com.hku.tripals.model.Event;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private static final String TAG = "ExploreFragment";
    private ExploreViewModel exploreViewModel;
    private SearchView searchbar;
    private CardView createEventCard;
    private CardView nearByCard;

    private List<Destination> destinationList = new ArrayList<>();
    private RecyclerView destinationRecyclerView;
    private LinearLayoutManager destinationLayoutManager;
    private DestinationAdapter destinationAdapter;

    private RecyclerView eventRecyclerView;
    private LinearLayoutManager eventLayoutManager;
    private EventAdapter eventAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        exploreViewModel =
                ViewModelProviders.of(this).get(ExploreViewModel.class);
        View root = inflater.inflate(R.layout.fragment_explore, container, false);
        createEventCard = root.findViewById(R.id.create_event_cardView);
        createEventCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CreateEventActivity.class);
                startActivity(intent);
            }
        });
        nearByCard = root.findViewById(R.id.nearby_cardView);
        nearByCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("lat", "");
                intent.putExtra("lng", "");
                startActivity(intent);
            }
        });
        destinationRecyclerView = root.findViewById(R.id.hot_destination_RecyclerView);
        destinationLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        destinationRecyclerView.setLayoutManager(destinationLayoutManager);
        destinationAdapter = new DestinationAdapter(getActivity());
        exploreViewModel.getDestinations().observe(getViewLifecycleOwner(), new Observer<List<Destination>>() {
            @Override
            public void onChanged(@Nullable List<Destination> destinationList) {
                destinationAdapter.setDestinationList(destinationList);
                destinationAdapter.notifyDataSetChanged();
            }
        });
        destinationRecyclerView.setAdapter(destinationAdapter);

        eventRecyclerView = root.findViewById(R.id.popular_events_RecyclerView);
        eventLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        eventRecyclerView.setLayoutManager(eventLayoutManager);
        eventAdapter = new EventAdapter(getActivity());
        exploreViewModel.getEvents().observe(getViewLifecycleOwner(), new Observer<List<Event>>() {
            @Override
            public void onChanged(List<Event> events) {
                eventAdapter.setEventList(events);
                eventAdapter.notifyDataSetChanged();
            }
        });
        exploreViewModel.loadEvent(10);
        eventRecyclerView.setAdapter(eventAdapter);

        searchbar = root.findViewById(R.id.explore_searchView);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchbar.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getActivity(), SearchResultsActivity.class)));
        searchbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchbar.setIconified(false);
            }
        });
        return root;
    }
}