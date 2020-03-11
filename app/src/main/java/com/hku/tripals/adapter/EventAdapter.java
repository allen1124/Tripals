package com.hku.tripals.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.hku.tripals.R;
import com.hku.tripals.model.Event;

import java.text.SimpleDateFormat;
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
        public TextView hostName;
        public TextView eventTitle;
        public TextView eventDescription;
        public TextView locationName;
        public TextView datetime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventCard = itemView.findViewById(R.id.event_recycler_cardView);
            eventImage = itemView.findViewById(R.id.event_recycler_imageView);
            hostImage = itemView.findViewById(R.id.event_host_avatar_recycler_imageView);
            hostName = itemView.findViewById(R.id.event_host_name_recycler_textView);
            eventTitle = itemView.findViewById(R.id.event_title_recycler_textView);
            eventDescription = itemView.findViewById(R.id.event_description_recycler_textView);
            locationName = itemView.findViewById(R.id.event_location_recycler_textView);
            datetime = itemView.findViewById(R.id.event_datetime_recycler_textView);
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
        holder.hostName.setText(event.getHostName());
        Glide.with(context)
                .load(event.getPhotoUrl())
                .into(holder.eventImage);
        holder.locationName.setText(event.getLocationName());
        holder.eventTitle.setText(event.getTitle());
        holder.eventDescription.setText(event.getDescription());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        holder.datetime.setText(simpleDateFormat.format(event.getDatetime()));
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
