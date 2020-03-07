package com.hku.tripals.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hku.tripals.MapsActivity;
import com.hku.tripals.R;
import com.hku.tripals.model.Destination;
import com.hku.tripals.model.Event;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private Context context;
    private List<Event> eventList;

    public EventAdapter(Context context){
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView eventCard;
        public ImageView eventImage;
        public ImageView hostImage;
        public TextView eventTitle;
        public TextView eventDescription;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventCard = itemView.findViewById(R.id.event_recycler_cardView);
            eventImage = itemView.findViewById(R.id.event_recycler_imageView);
            hostImage = itemView.findViewById(R.id.event_host_recycler_imageView);
            eventTitle = itemView.findViewById(R.id.event_title_recycler_textView);
            eventDescription = itemView.findViewById(R.id.event_description_recycler__textView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_event, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Event event = eventList.get(position);
        Glide.with(context)
                .load(event.getHostAvatarUrl())
                .circleCrop()
                .into(holder.hostImage);
        Glide.with(context)
                .load(event.getPhotoUrl())
                .into(holder.eventImage);
        holder.eventTitle.setText(event.getTitle());
        holder.eventDescription.setText(event.getDescription());
    }

    @Override
    public int getItemCount() {
        if(eventList == null)
            return 0;
        return eventList.size();
    }

    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }
}
