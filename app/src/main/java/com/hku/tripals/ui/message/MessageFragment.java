package com.hku.tripals.ui.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hku.tripals.R;
import com.hku.tripals.adapter.ChatAdapter;
import com.hku.tripals.model.EventChat;

import java.util.ArrayList;
import java.util.List;

public class MessageFragment extends Fragment {

    private MessageViewModel messageViewModel;

    private View ChatsView;
    private RecyclerView ChatsList;

    private List<EventChat> chatList = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private LinearLayoutManager linearLayoutManager;

    private DatabaseReference eventChats;
    private FirebaseAuth mAuth;
    String currentUserID;

    public MessageFragment(){
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if(currentUserID == null){
            messageViewModel =
                    ViewModelProviders.of(this).get(MessageViewModel.class);
            View ChatsView = inflater.inflate(R.layout.fragment_message, container, false);
            final TextView textView = ChatsView.findViewById(R.id.text_message);
            messageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    textView.setText(s);
                }
            });
        } else {
            eventChats = FirebaseDatabase.getInstance().getReference().child("chats");

            ChatsView = inflater.inflate(R.layout.fragment_message, container, false);
            ChatsList = (RecyclerView) ChatsView.findViewById(R.id.chats_List);
            linearLayoutManager = new LinearLayoutManager(getActivity());
            ChatsList.setLayoutManager(linearLayoutManager);
            chatAdapter = new ChatAdapter(getActivity(), chatList);

            eventChats.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    if (dataSnapshot.exists()){
                        EventChat eventChat = dataSnapshot.getValue(EventChat.class);
                        chatList.add(eventChat);
                        chatAdapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

            ChatsList.setAdapter(chatAdapter);

//            ChatsView = inflater.inflate(R.layout.fragment_message, container, false);
//            ChatsList = (RecyclerView) ChatsView.findViewById(R.id.chats_List);
//            ChatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        return ChatsView;
    }
//        @Override
//    public void onStart() {
//        super.onStart();
//        //eventsDatabase = FirebaseDatabase.getInstance().getReference().child("events");
//        //eventsRef = FirebaseDatabase.getInstance().getReference().child("events");
//        //eventsRef = FirebaseDatabase.getInstance().getReference().child("events").child(currentUserID);
//        //usersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
//        //eventsDatabase = FirebaseDatabase.getInstance().getReference().child("events").child("currentUserID);
//
//        FirebaseRecyclerOptions<EventChat> options =
//                new FirebaseRecyclerOptions.Builder<EventChat>()
//                        .setQuery(eventChats, EventChat.class)
//                        .build();
//
//        FirebaseRecyclerAdapter<EventChat, ChatsViewHolder> ChatsAdapter =
//                new FirebaseRecyclerAdapter<EventChat, ChatsViewHolder>(options) {
//                    @Override
//                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull EventChat model) {
//                        final String eventIDs = getRef(position).getKey();
//                        eventChats.child(eventIDs).addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                if (dataSnapshot.exists()){
//                                    if (dataSnapshot.hasChild("participants")){
//                                        Iterator iterator = dataSnapshot.child("participants").getChildren().iterator();
//                                        while(iterator.hasNext()){
//                                            String parti_ID = (String) ((DataSnapshot)iterator.next()).getValue();
//                                            if (currentUserID.equals(parti_ID)){
//                                                holder.itemView.setVisibility(View.VISIBLE);
//                                                holder.previewChat.setText(parti_ID);
//                                                if (dataSnapshot.hasChild("eventPhotoUrl")){
//                                                    String profileimage = dataSnapshot.child("eventPhotoUrl").getValue().toString();
//                                                    if (profileimage.isEmpty()){
//                                                        profileimage = "R.color.colorPrimary";
//                                                    }
//                                                    Picasso.get().load(profileimage).placeholder(R.color.colorPrimary).into(holder.icon_image);
//                                                }
//                                                final String eventTitle = dataSnapshot.child("eventTitle").getValue().toString();
//                                                final String event_image = holder.icon_image.toString();
//                                                holder.eventName.setText(eventTitle);
//                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(View view) {
//                                                        Intent chatIntent = new Intent(getContext(), MessageActivity.class);
//                                                        chatIntent.putExtra("eventID", eventIDs);
//                                                        chatIntent.putExtra("eventName", eventTitle);
//                                                        chatIntent.putExtra("eventImage", event_image);
//                                                        startActivity(chatIntent);
//                                                    }
//                                                });
//                                                break;
//                                            } else {
//                                                holder.itemView.setVisibility(View.GONE);
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//                            }
//                        });
//
//                        //Query lastQuery = eventsDatabase.orderByKey().limitToLast(1);
////                        Query lastQuery = eventsDatabase.orderByKey().limitToFirst(1);
////                        lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
////                            @Override
////                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                                Log.d("msg: ", dataSnapshot.getValue().toString());
////                                if (dataSnapshot.hasChild("message")){
////                                    String message = dataSnapshot.child("message").getValue().toString();
////                                    holder.previewChat.setText(message);
////                                } else {
////                                    String message = "No message yet...";
////                                    holder.previewChat.setText(message);
////                                }
////                            }
////                            @Override
////                            public void onCancelled(@NonNull DatabaseError databaseError) {
////                            }
////                        });
//                    };
//                    @NonNull
//                    @Override
//                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_chats, parent, false);
//                        return new ChatsViewHolder(view);
//                    }
//                };
//        ChatsList.setAdapter(ChatsAdapter);
//        ChatsAdapter.startListening();
//    }
//
//    public class ChatsViewHolder extends RecyclerView.ViewHolder {
//        public TextView eventName;
//        public CircleImageView icon_image;
//        public TextView previewChat;
//
//        public ChatsViewHolder(@NonNull View itemView) {
//            super(itemView);
//            icon_image = itemView.findViewById(R.id.circleEventImageView);
//            eventName = itemView.findViewById(R.id.event_name_textview);
//            previewChat = itemView.findViewById(R.id.chatpreview_textview);
//        }
//    }
}