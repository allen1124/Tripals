package com.hku.tripals.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hku.tripals.R;
import com.hku.tripals.model.Message;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public static final int MSG_TYPE_RECEIVE = 0;
    public static final int MSG_TYPE_SEND = 1;

    private Context mContext;
    private List<Message> userMsgList;
    private FirebaseAuth mAuth;
    FirebaseUser current_user;
    private DatabaseReference userRef, eventRef;

    public MessageAdapter(Context mContext, List<Message> userMsgList){
        this.userMsgList = userMsgList;
        this.mContext = mContext;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView msg_show, sender_name, msg_date_time;
        public CircleImageView profile_image;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_image = (CircleImageView) itemView.findViewById(R.id.profile_image);
            msg_show = (TextView) itemView.findViewById(R.id.msg_show);
            sender_name = (TextView) itemView.findViewById(R.id.sender_name);
            msg_date_time = (TextView) itemView.findViewById(R.id.msg_date_time);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();
        if (viewType == MSG_TYPE_SEND){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_chat_send, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_chat_receive, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String senderID = mAuth.getCurrentUser().getUid();
        Message msg = userMsgList.get(position);

        String fromUserID = msg.getSenderID();
        //String fromMessageType = msg.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){
                    String receiveImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiveImage).placeholder(R.drawable.ic_profile_black_24dp).into(holder.profile_image);
                }
                holder.profile_image.setImageResource(R.drawable.ic_profile_black_24dp);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        String msg_time = msg.getMsgDate() + " " + msg.getMsgTime();
        if (fromUserID.equals(senderID)){
            holder.msg_show.setText(msg.getMsgText());
            holder.msg_date_time.setText(msg_time);
        } else {
            holder.msg_show.setText(msg.getMsgText());
            holder.sender_name.setText(msg.getSenderID());
            holder.msg_date_time.setText(msg_time);
        }
    }

    @Override
    public int getItemCount() {
        if(userMsgList == null)
            return 0;
        return userMsgList.size();
    }

    @Override
    public int getItemViewType(int position) {
        current_user = FirebaseAuth.getInstance().getCurrentUser();
        if (userMsgList.get(position).getSenderID().equals(current_user.getUid())){
            return MSG_TYPE_SEND;
        } else {
            return MSG_TYPE_RECEIVE;
        }
    }
}
