package com.hku.tripals.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hku.tripals.FullScreenImageActivity;
import com.hku.tripals.R;
import com.hku.tripals.UserProfileActivity;
import com.hku.tripals.model.Comment;
import com.hku.tripals.model.User;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>{

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
    private ArrayList<Comment> commentList;
    Context context;

    private FirebaseFirestore db;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public CommentsAdapter(Context context) {
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar;
        public TextView username;
        public TextView comment;
        public ImageView commentPhoto;
        public TextView pettyTime;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.c_avatar_imageView);
            username = (TextView) itemView.findViewById(R.id.c_username_textView);
            comment = (TextView) itemView.findViewById(R.id.c_user_comment_textView);
            commentPhoto = (ImageView) itemView.findViewById(R.id.c_comment_imageView);
            pettyTime = (TextView) itemView.findViewById(R.id.c_petty_time_textView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_comment, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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
        preGoToUser();

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!comment.getUserId().matches(currentUser.getUid())) {
                    //holder.username.setEnabled(false);
                    //holder.commentPhoto.setEnabled(false);
                    goToUser(comment.getUserId());
                    //holder.username.setEnabled(true);
                    //holder.commentPhoto.setEnabled(true);
                }
            }
        });

        holder.avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!comment.getUserId().matches(currentUser.getUid())) {
                    //holder.username.setEnabled(false);
                    //holder.commentPhoto.setEnabled(false);
                    goToUser(comment.getUserId());
                    //holder.username.setEnabled(true);
                    //holder.commentPhoto.setEnabled(true);
                }
            }
        });

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
                Log.d("CommentsAdapter: ", "onClick: go user detail :" + user.getUid());
                Intent myIntent = new Intent(context, UserProfileActivity.class);
                myIntent.putExtra("user", (Serializable) user);
                context.startActivity(myIntent);
                ((Activity) context).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }
}
