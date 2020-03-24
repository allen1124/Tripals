package com.hku.tripals.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.hku.tripals.FullScreenImageActivity;
import com.hku.tripals.R;
import com.hku.tripals.model.Message;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    public static final int MSG_TYPE_RECEIVE = 0;
    public static final int MSG_TYPE_SEND = 1;
    public static final int MSG_TYPE_JOIN = 2;

    public String msg_send = "";
    public String msg_receive = "";

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
        public TextView msg_show, sender_name, msg_date_time, msg_date_time_2;
        public TextView msg_join;
        public ImageView msg_show_img;
        public CircleImageView profile_image;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_image = (CircleImageView) itemView.findViewById(R.id.profile_image);
            msg_show_img = (ImageView) itemView.findViewById(R.id.msg_show_img);
            msg_show = (TextView) itemView.findViewById(R.id.msg_show);
            msg_join = (TextView) itemView.findViewById(R.id.msg_join);
            sender_name = (TextView) itemView.findViewById(R.id.sender_name);
            msg_date_time = (TextView) itemView.findViewById(R.id.msg_date_time);
            msg_date_time_2 = (TextView) itemView.findViewById(R.id.msg_date_time_2);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();
        if (viewType == MSG_TYPE_SEND){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_chat_send, parent, false);
            return new MessageViewHolder(view);
        } else if (viewType == MSG_TYPE_RECEIVE){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_chat_receive, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_chat_joined, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String senderID = mAuth.getCurrentUser().getUid();
        Message msg = userMsgList.get(position);

        String fromUserID = msg.getSenderID();
        String fromMessageType = msg.getmsgType();

        if (msg_send.equals("1") && msg_receive.equals("1")){
            final String receiveImage = msg.getSenderURL();
            Picasso
                    .get()
                    .load(receiveImage)
                    .fetch(new Callback(){
                        @Override
                        public void onSuccess() {
                            Picasso
                                    .get()
                                    .load(receiveImage)
                                    .placeholder(R.drawable.ic_profile_black_24dp)
                                    .into(holder.profile_image);
                        }
                        @Override
                        public void onError(Exception e) { }
                    });
            //Picasso.get().load(receiveImage).placeholder(R.drawable.ic_profile_black_24dp).into(holder.profile_image);
        }

        String msg_time = msg.getMsgDate() + " " + msg.getMsgTime();

        if(fromMessageType.equals("text")){
            holder.msg_show_img.setVisibility(View.GONE);
            holder.msg_date_time_2.setVisibility(View.GONE);

            if (fromUserID.equals(senderID)){
                holder.msg_show.setText(msg.getMsgText());
                holder.msg_date_time.setText(msg_time);
            } else {
                holder.msg_show.setText(msg.getMsgText());
                holder.sender_name.setText(msg.getSenderName());
                holder.msg_date_time.setText(msg_time);
            }
        } else if (fromMessageType.equals("image")){
            holder.msg_show.setVisibility(View.GONE);
            holder.msg_date_time.setVisibility(View.GONE);

            final Uri imageUri = Uri.parse(msg.getMsgText());
            holder.msg_show_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent imageIntent = new Intent(mContext, FullScreenImageActivity.class);
                    imageIntent.putExtra("imageUri", imageUri);
                    mContext.startActivity(imageIntent);
                }
            });

            if (fromUserID.equals(senderID)){
                Picasso.get().load(msg.getMsgText()).into(holder.msg_show_img);
                holder.msg_show_img.setVisibility(View.VISIBLE);
                holder.msg_show.setText(msg.getMsgText());
                holder.msg_date_time_2.setText(msg_time);
            } else {
                Picasso.get().load(msg.getMsgText()).into(holder.msg_show_img);
                holder.msg_show_img.setVisibility(View.VISIBLE);
                holder.sender_name.setText(msg.getSenderName());
                holder.msg_date_time_2.setText(msg_time);
            }
        }
        else if (fromMessageType.equals("join")){
//            holder.msg_show_img.setVisibility(View.GONE);
//            holder.msg_date_time_2.setVisibility(View.GONE);
//            holder.msg_show.setVisibility(View.GONE);
//            holder.msg_date_time.setVisibility(View.GONE);

            holder.msg_join.setText(msg.getMsgText());
            holder.msg_join.setVisibility(View.VISIBLE);
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
        if (userMsgList.get(position).getmsgType().equals("join")){
            msg_send = "0";
            msg_receive = "0";
            return MSG_TYPE_JOIN;
        }
        if (userMsgList.get(position).getSenderID().equals(current_user.getUid())){
            msg_send = "1";
            return MSG_TYPE_SEND;
        } else {
            msg_receive = "1";
            return MSG_TYPE_RECEIVE;
        }
    }
}
