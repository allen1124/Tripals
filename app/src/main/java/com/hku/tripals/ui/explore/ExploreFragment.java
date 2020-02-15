package com.hku.tripals.ui.explore;

import android.os.Bundle;
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
import com.hku.tripals.adapter.DestinationAdapter;
import com.hku.tripals.model.Destination;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    private static final String TAG = "ExploreFragment";
    private ExploreViewModel exploreViewModel;
    private List<Destination> destinationList = new ArrayList<>();
    private RecyclerView destinationRecyclerView;
    private LinearLayoutManager destinationLayoutManager;
    private DestinationAdapter destinationAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        exploreViewModel =
                ViewModelProviders.of(this).get(ExploreViewModel.class);
        View root = inflater.inflate(R.layout.fragment_explore, container, false);
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
        return root;
    }
}