package com.hku.tripals.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hku.tripals.MapsActivity;
import com.hku.tripals.R;
import com.hku.tripals.model.Destination;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.ViewHolder> {

    private Context context;
    private List<Destination> destinationList;

    public DestinationAdapter(Context context) {
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView destinationImage;
        public TextView destinationName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            destinationImage = itemView.findViewById(R.id.destination_imageView);
            destinationName = itemView.findViewById(R.id.destination_textView);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_destination, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Destination destination = destinationList.get(position);
        holder.destinationName.setText(context.getText(destination.getName()));
        holder.destinationImage.setImageResource(destination.getImage());
        //Click to MapsActivity and Focus at that city, To-Do
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MapsActivity.class);
                intent.putExtra("lat", destination.getLatitude());
                intent.putExtra("lng", destination.getLongitude());
                context.startActivity(intent);
        }
        });
    }

    @Override
    public int getItemCount() {
        if(destinationList == null)
            return 0;
        return destinationList.size();
    }

    public void setDestinationList(List<Destination> destinationList) {
        this.destinationList = destinationList;
    }
}
