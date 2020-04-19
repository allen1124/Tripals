package com.hku.tripals.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hku.tripals.FullScreenImageActivity;
import com.hku.tripals.R;
import com.hku.tripals.model.Comment;

import org.ocpsoft.prettytime.PrettyTime;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>{

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
    private ArrayList<Comment> commentList;
    Context context;

    private FirebaseAuth mAuth;
    private String currentUserID;
    private FirebaseFirestore db;

    public CommentsAdapter(Context context) {
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar;
        public TextView username;
        public TextView comment;
        public ImageView commentPhoto;
        public TextView pettyTime;
        public TextView commentPin;
        public CardView commentCard;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.c_avatar_imageView);
            username = (TextView) itemView.findViewById(R.id.c_username_textView);
            comment = (TextView) itemView.findViewById(R.id.c_user_comment_textView);
            commentPhoto = (ImageView) itemView.findViewById(R.id.c_comment_imageView);
            pettyTime = (TextView) itemView.findViewById(R.id.c_petty_time_textView);
            commentPin = (TextView) itemView.findViewById(R.id.c_pin_textView);
            commentCard = (CardView) itemView.findViewById(R.id.user_comment_cardView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_comment, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Comment comment = commentList.get(position);
        if(comment.getUserPhoto() != null) {
            Glide.with(context).load(Uri.parse(comment.getUserPhoto())).apply(RequestOptions.circleCropTransform()).into(holder.avatar);
        }
        if(comment.getCommentPhoto() == null || comment.getCommentPhoto().matches("")){
            holder.commentPhoto.setVisibility(View.GONE);
        }else{
            holder.commentPhoto.setVisibility(View.VISIBLE);
            Glide.with(context).load(comment.getCommentPhoto()).into(holder.commentPhoto);
            final Uri imageUri = Uri.parse(comment.getCommentPhoto());
            holder.commentPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent imageIntent = new Intent(context, FullScreenImageActivity.class);
                    imageIntent.putExtra("imageUri", imageUri);
                    context.startActivity(imageIntent);
                }
            });
        }
        holder.username.setText(comment.getUsername());
        holder.comment.setText(comment.getComment());
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        holder.pettyTime.setText(prettyTime.format(comment.getTimestamp()));

        if(comment.getHostId() != null){
            if(comment.getHostId().matches(currentUserID)){
                holder.commentPin.setVisibility(View.VISIBLE);
                if(comment.getHighlighted().matches("NO")){
                    holder.commentPin.setText("Highlight");
                } else {
                    holder.commentPin.setText("Highlighted");
                    holder.commentCard.setCardBackgroundColor(Color.parseColor("#d4f6ff"));
                }
                holder.commentPin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(holder.commentPin.getText().toString().matches("Highlight")){
                            holder.commentPin.setText("Highlighted");
                            holder.commentCard.setCardBackgroundColor(Color.parseColor("#d4f6ff"));
                            comment.setHighlighted("YES");
                            db.collection("events").document(comment.getEventId()).collection("comments").document(comment.getCommentId()).update("highlighted", "YES");

                        } else {
                            holder.commentPin.setText("Highlight");
                            holder.commentCard.setCardBackgroundColor(Color.parseColor("#ffffff"));
                            comment.setHighlighted("NO");
                            db.collection("events").document(comment.getEventId()).collection("comments").document(comment.getCommentId()).update("highlighted", "NO");
                        }
                    }
                });
            } else {
                holder.commentPin.setVisibility(View.GONE);
                if(comment.getHighlighted().matches("NO")){
                    holder.commentCard.setCardBackgroundColor(Color.parseColor("#ffffff"));
                } else {
                    holder.commentCard.setCardBackgroundColor(Color.parseColor("#d4f6ff"));
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if(commentList == null)
            return 0;
        return commentList.size();
    }

    public void setCommentList(ArrayList<Comment> commentList) {
        this.commentList = commentList;
    }
}
