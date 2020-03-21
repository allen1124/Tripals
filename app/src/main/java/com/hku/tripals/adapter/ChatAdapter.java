package com.hku.tripals.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hku.tripals.R;
import com.hku.tripals.model.EventChat;
import com.hku.tripals.ui.message.MessageActivity;
import com.squareup.picasso.Picasso;

import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context mContext;
    private List<EventChat> chatList;
    private FirebaseAuth mAuth;
    String currentUserID;
    private DatabaseReference eventChats;

    public ChatAdapter(Context mContext, List<EventChat> chatList){
        this.mContext = mContext;
        this.chatList = chatList;
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder{
        public TextView eventName;
        public CircleImageView icon_image;
        public TextView previewChat;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            icon_image = itemView.findViewById(R.id.circleEventImageView);
            eventName = itemView.findViewById(R.id.event_name_textview);
            previewChat = itemView.findViewById(R.id.chatpreview_textview);
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_chats, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ChatViewHolder holder, int position) {
        final EventChat chat = chatList.get(position);
        String eventid = chat.getEventId();
        eventChats = FirebaseDatabase.getInstance().getReference().child("chats").child(eventid);

        eventChats.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("participants")){
                        Iterator iterator = dataSnapshot.child("participants").getChildren().iterator();
                        while(iterator.hasNext()){
                            String parti_ID = (String) ((DataSnapshot)iterator.next()).getValue();
                            if (currentUserID.equals(parti_ID)){
                                holder.itemView.setVisibility(View.VISIBLE);
                                holder.previewChat.setText(parti_ID);
                                if (dataSnapshot.hasChild("eventPhotoUrl")){
                                    String profileimage = dataSnapshot.child("eventPhotoUrl").getValue().toString();
                                    if (profileimage.isEmpty()){
                                        profileimage = "R.color.colorPrimary";
                                    }
                                    Picasso.get().load(profileimage).placeholder(R.color.colorPrimary).into(holder.icon_image);
                                }
                                //final String eventTitle = dataSnapshot.child("eventTitle").getValue().toString();
                                final String event_image = holder.icon_image.toString();
                                //final String eventIDs = dataSnapshot.child("eventId").getValue().toString();
                                //holder.eventName.setText(eventTitle);
                                holder.eventName.setText(chat.getEventTitle());
                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent chatIntent = new Intent(mContext, MessageActivity.class);
                                        chatIntent.putExtra("eventID", chat.getEventId());
                                        chatIntent.putExtra("eventName", chat.getEventTitle());
                                        chatIntent.putExtra("eventImage", event_image);
                                        mContext.startActivity(chatIntent);
                                    }
                                });
                                break;
                            } else {
                                holder.itemView.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @Override
    public int getItemCount() {
        if(chatList == null)
            return 0;
        return chatList.size();
    }

    public void setEventChats(List<EventChat> chat){
        this.chatList = chat;
        notifyDataSetChanged();
    }
}
