package com.hku.tripals.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hku.tripals.EventActivity;
import com.hku.tripals.R;
import com.hku.tripals.UserProfileActivity;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.User;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private static final String TAG = "EventAdapter";
    private Activity context;
    private List<Event> eventList;

    private FirebaseFirestore db;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public EventAdapter(Activity context){
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView eventCard;
        public ImageView eventImage;
        public ImageView hostImage;
        public TextView hostName;
        public TextView datetime;
        public TextView eventTitle;
        public TextView eventDescription;
        public TextView locationName;
        public TextView quota;
        public TextView timestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventCard = itemView.findViewById(R.id.event_recycler_cardView);
            eventImage = itemView.findViewById(R.id.event_recycler_imageView);
            hostImage = itemView.findViewById(R.id.event_host_avatar_recycler_imageView);
            hostName = itemView.findViewById(R.id.event_host_name_recycler_textView);
            eventTitle = itemView.findViewById(R.id.event_title_recycler_textView);
            datetime = itemView.findViewById(R.id.event_datetime_recycler_textView);
            eventDescription = itemView.findViewById(R.id.event_description_recycler_textView);
            locationName = itemView.findViewById(R.id.event_location_recycler_textView);
            quota = itemView.findViewById(R.id.event_quota_recycler_textView);
            timestamp = itemView.findViewById(R.id.event_timestamp_recycler_textView);
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
        Log.d(TAG, "onBindViewHolder: getQuota: "+event.getQuota());
        if(event.getOpenness().matches("OPEN") && event.getQuota() != -1){
            int noPanticipant = 0;
            if(event.getParticipants() != null)
                noPanticipant = event.getParticipants().size();
            String quotaLeft = String.valueOf(event.getQuota()-noPanticipant);
            holder.quota.setText(quotaLeft+" "+context.getString(R.string.left));
        }else{
            Log.d(TAG, "onBindViewHolder: NO QUOTA");
            holder.quota.setVisibility(View.GONE);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        holder.datetime.setText(simpleDateFormat.format(event.getDatetime()));
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        holder.timestamp.setText(prettyTime.format(event.getTimestamp()));
        preGoToUser();

        holder.hostName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: go User Profile :" + event.getHost());
                if(!event.getHost().matches(currentUser.getUid())) {
                    holder.hostName.setEnabled(false);
                    holder.hostImage.setEnabled(false);
                    goToUser(event.getHost());
                    holder.hostName.setEnabled(true);
                    holder.hostImage.setEnabled(true);
                }
            }
        });

        holder.hostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: go User Profile :" + event.getHost());
                if(!event.getHost().matches(currentUser.getUid())) {
                    holder.hostName.setEnabled(false);
                    holder.hostImage.setEnabled(false);
                    goToUser(event.getHost());
                    holder.hostName.setEnabled(true);
                    holder.hostImage.setEnabled(true);
                }
            }
        });

        holder.eventCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: go event detail :" + event.getId());
                Intent myIntent = new Intent(context, EventActivity.class);
                myIntent.putExtra("event", (Serializable) event);
                context.startActivity(myIntent);
                context.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

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

    private void preGoToUser() {
        db = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void goToUser(String uid){
        db.collection("user-profile").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                Log.d(TAG, "onClick: go user detail :" + user.getUid());
                Intent myIntent = new Intent(context, UserProfileActivity.class);
                myIntent.putExtra("user", (Serializable) user);
                context.startActivity(myIntent);
                context.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }
}
