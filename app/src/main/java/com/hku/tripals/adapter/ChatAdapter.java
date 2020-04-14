package com.hku.tripals.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.hku.tripals.R;
import com.hku.tripals.model.EventChat;
import com.hku.tripals.ui.message.MessageActivity;

import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private Context mContext;
    private List<EventChat> chatList;
    private FirebaseAuth mAuth;
    String currentUserID;

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
        final int user_check;
        if(chat.getParticipants().get(1).equals(currentUserID)){
            user_check = 0;
        } else {
            user_check = 1;
        }
        if(chat.getEventTitle() == null) {
            holder.eventName.setText(chat.getParticipantName().get(user_check));
        } else {
            holder.eventName.setText(chat.getEventTitle());
        }
        if(chat.getEventPhotoUrl() == null){
            Glide.with(mContext)
                    .load(chat.getParticipantPhotoUrl().get(user_check))
                    .into(holder.icon_image);
        } else {
            Glide.with(mContext)
                    .load(chat.getEventPhotoUrl())
                    .into(holder.icon_image);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatIntent = new Intent(mContext, MessageActivity.class);
                if(chat.getType().matches("EVENT")){
                    chatIntent.putExtra("eventID", chat.getEventId());
                    chatIntent.putExtra("eventName", chat.getEventTitle());
                    chatIntent.putExtra("eventImage", chat.getEventPhotoUrl());
                    chatIntent.putExtra("type", chat.getType());
                    chatIntent.putExtra("participants", String.valueOf(chat.getParticipants()));
                }
                else if(chat.getType().matches("INDIVIDUAL")){
                    chatIntent.putExtra("eventID", chat.getEventId());
                    chatIntent.putExtra("eventName", chat.getParticipantName().get(user_check));
                    chatIntent.putExtra("eventImage", chat.getParticipantPhotoUrl().get(user_check));
                    chatIntent.putExtra("type", chat.getType());
                    chatIntent.putExtra("targetUID", chat.getParticipants().get(user_check));
                }
                mContext.startActivity(chatIntent);
            }
        });
        if(chat.getLastestMsg() != null && chat.getLastestMsg().startsWith("https://firebase")){
            holder.previewChat.setText("Image");
        } else {
            holder.previewChat.setText(chat.getLastestMsg());
        }
    }

    @Override
    public int getItemCount() {
        if(chatList == null)
            return 0;
        return chatList.size();
    }

    public void setEventChats(List<EventChat> chat){
        this.chatList = chat;
    }
}
