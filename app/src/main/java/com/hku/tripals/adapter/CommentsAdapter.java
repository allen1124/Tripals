package com.hku.tripals.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hku.tripals.R;
import com.hku.tripals.model.Comment;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>{

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
    private ArrayList<Comment> commentList;
    Context context;

    public CommentsAdapter(Context context) {
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar;
        public TextView username;
        public TextView comment;
        public TextView pettyTime;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.c_avatar_imageView);
            username = (TextView) itemView.findViewById(R.id.c_username_textView);
            comment = (TextView) itemView.findViewById(R.id.c_user_comment_textView);
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
        holder.username.setText(comment.getUsername());
        holder.comment.setText(comment.getComment());
        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        holder.pettyTime.setText(prettyTime.format(comment.getTimestamp()));
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
