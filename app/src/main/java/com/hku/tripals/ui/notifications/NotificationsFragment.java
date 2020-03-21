package com.hku.tripals.ui.notifications;

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
import com.hku.tripals.adapter.EventAdapter;
import com.hku.tripals.adapter.RequestAdapter;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Request;

import java.util.List;

public class NotificationsFragment extends Fragment {

    private static final String TAG = "NotificationsFragment";
    private NotificationsViewModel notificationsViewModel;

    private RecyclerView requestRecyclerView;
    private TextView noRequest;
    private LinearLayoutManager requestLayoutManager;
    private RequestAdapter requestAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                ViewModelProviders.of(this).get(NotificationsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        requestRecyclerView = root.findViewById(R.id.request_RecyclerView);
        noRequest = root.findViewById(R.id.no_request_textView);
        requestLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        requestRecyclerView.setLayoutManager(requestLayoutManager);
        requestAdapter = new RequestAdapter(getActivity());
        notificationsViewModel.getRequest().observe(getViewLifecycleOwner(), new Observer<List<Request>>() {
            @Override
            public void onChanged(List<Request> requests) {
                requestAdapter.setRequestList(requests);
                requestAdapter.notifyDataSetChanged();
                if(requestAdapter.getItemCount() > 0)
                    noRequest.setVisibility(View.GONE);
                else
                    noRequest.setVisibility(View.VISIBLE);
            }
        });
        notificationsViewModel.loadRequest();
        requestRecyclerView.setAdapter(requestAdapter);
        return root;
    }
}