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

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private static final String TAG = "UserAdapter";
    private Activity context;
    private List<User> userList;

    public UserAdapter(Activity context){
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CardView userCard;
        public ImageView userImage;
        public TextView country;
        public TextView userTitle;
        public TextView userDescription;
        public TextView gender;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userCard = itemView.findViewById(R.id.user_recycler_cardView);
            userImage = itemView.findViewById(R.id.user_recycler_imageView);
            userTitle = itemView.findViewById(R.id.user_title_recycler_textView);
            country = itemView.findViewById(R.id.user_country_recycler_textView);
            userDescription = itemView.findViewById(R.id.user_description_recycler_textView);
            //gender = itemView.findViewById(R.id.user_gender_recycler_textView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_user, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final User user = userList.get(position);
        Glide.with(context)
                .load(user.getAvatarImageUrl())
                .into(holder.userImage);
        //holder.gender.setText(user.getGender());
        holder.userTitle.setText(user.getDisplayName());
        holder.userDescription.setText(user.getBio());
        holder.country.setText(user.getHomeCountry());
        holder.userCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: go event detail :" + user.getUid());
                Intent myIntent = new Intent(context, UserProfileActivity.class);
                myIntent.putExtra("user", (Serializable) user);
                context.startActivity(myIntent);
                context.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(userList == null)
            return 0;
        return userList.size();
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }
}
