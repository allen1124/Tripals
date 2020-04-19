package com.hku.tripals.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hku.tripals.EventActivity;
import com.hku.tripals.R;
import com.hku.tripals.TripActivity;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Trip;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    private static final String TAG = "TripAdapter";
    private Activity context;
    private List<Trip> tripList;

    public TripAdapter(Activity context){
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView tripCard;
        public ImageView tripImage;
        public TextView tripTitle;
        public TextView tripDestination;
        public TextView timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tripCard = itemView.findViewById(R.id.trip_recycler_cardView);
            tripImage = itemView.findViewById(R.id.trip_recycler_imageView);
            tripTitle = itemView.findViewById(R.id.trip_title_recycler_textView);
            tripDestination = itemView.findViewById(R.id.trip_destination_recycler_textView);
            timestamp = itemView.findViewById(R.id.trip_timestamp_recycler_textView);
        }
    }

    @NonNull
    @Override
    public TripAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_trip, parent, false);
        TripAdapter.ViewHolder holder = new TripAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final TripAdapter.ViewHolder holder, int position) {
        final Trip trip = tripList.get(position);
        Glide.with(context)
                .load(trip.getPhotoUrl())
                .into(holder.tripImage);
        holder.tripTitle.setText(trip.getTitle());
        holder.tripDestination.setText(trip.getDestination());
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        holder.timestamp.setText(prettyTime.format(trip.getTimestamp()));

        holder.tripCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: go trip detail :" + trip.getId());
                Intent myIntent = new Intent(context, TripActivity.class);
                myIntent.putExtra("trip", (Serializable) trip);
                context.startActivity(myIntent);
                context.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(tripList == null)
            return 0;
        return tripList.size();
    }

    public void setTripList(List<Trip> tripList) {
        this.tripList = tripList;
    }
}
