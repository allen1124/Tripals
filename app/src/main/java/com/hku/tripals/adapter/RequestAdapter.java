package com.hku.tripals.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.hku.tripals.NotificationService.APIService;
import com.hku.tripals.NotificationService.Client;
import com.hku.tripals.NotificationService.Data;
import com.hku.tripals.NotificationService.Response;
import com.hku.tripals.NotificationService.Sender;
import com.hku.tripals.NotificationService.Token;
import com.hku.tripals.R;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Request;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder>{

    private static final String TAG = "RequestAdapter";
    private Activity context;
    private List<Request> requestList;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    private DatabaseReference eventMsgRef, EventMsgKeyRef;
    private String currentDate, currentTime;

    private APIService apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);;

    public RequestAdapter(Activity context){
        this.context = context;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView eventPhoto;
        public TextView eventTitle;
        public TextView eventQuota;
        public ImageView requestorAvatar;
        public TextView requestorName;
        public Button acceptButton;
        public Button rejectButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventPhoto = itemView.findViewById(R.id.request_event_recycler_imageView);
            eventTitle = itemView.findViewById(R.id.request_event_title_recycler_textView);
            eventQuota = itemView.findViewById(R.id.request_event_quota_recycler_textView);
            requestorAvatar = itemView.findViewById(R.id.requeset_avatar_recycler_imageView);
            requestorName = itemView.findViewById(R.id.request_name_recycler_textView);
            acceptButton = itemView.findViewById(R.id.request_accept_button);
            rejectButton = itemView.findViewById(R.id.request_reject_button);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_request, parent, false);
        RequestAdapter.ViewHolder holder = new RequestAdapter.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Request request = requestList.get(position);
        Glide.with(context)
                .load(request.getRequestorAvatar())
                .circleCrop()
                .into(holder.requestorAvatar);
        Glide.with(context)
                .load(request.getEventPhotoUrl())
                .into(holder.eventPhoto);
        holder.eventTitle.setText(request.getEventTitle());
        if(request.getQuota() == -1){
            holder.eventQuota.setVisibility(View.GONE);
        }else {
            String quotaLeft = String.valueOf(request.getQuota() - request.getParticipantSize());
            holder.eventQuota.setText(quotaLeft + " " + context.getString(R.string.left));
        }
        holder.requestorName.setText(request.getRequestorName());
        holder.rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("requests").document(request.getRequestorUid()+'-'+request.getEventId())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                            }
                        });
                sendNotification(request.getRequestorUid(), request.getEventId(), "Request Rejected", "You are rejected to join "+request.getEventTitle());
                Toast.makeText(context, "Request rejected.",
                        Toast.LENGTH_LONG).show();
            }
        });
        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accecptRequest(request);
                Toast.makeText(context, "Request accepted.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        if(requestList == null)
            return 0;
        return requestList.size();
    }

    public void setRequestList(List<Request> requestList) {
        this.requestList = requestList;
    }

    private void accecptRequest(final Request request){
        Log.d(TAG, "accecptRequest: called");
        DocumentReference eventRef = db.collection("events").document(request.getEventId());
        eventRef.update("participants", FieldValue.arrayUnion(request.getRequestorUid()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
        if(request.getQuota() != -1 && request.getQuota()-request.getParticipantSize()-1 == 0) {
            eventRef.update("openness", "CLOSED");
        }
        db.collection("requests").whereEqualTo("eventId", request.getEventId()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {
                        if(request.getQuota() != -1 && request.getQuota()-request.getParticipantSize()-1 == 0) {
                            db.collection("requests").document(document.getId()).delete();
                        }else {
                            db.collection("requests").document(document.getId()).update(
                                    "participantSize", FieldValue.increment(1)
                            );
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
        eventRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Event event = documentSnapshot.toObject(Event.class);
                List<String> participant = event.getParticipants();
                participant.add(event.getHost());
                HashMap<String, Object> chats = new HashMap<>();
                chats.put("eventId", request.getEventId());
                chats.put("host", request.getHostUid());
                chats.put("eventPhotoUrl", request.getEventPhotoUrl());
                chats.put("eventTitle", request.getEventTitle());
                chats.put("participants", participant);
                db.collection("chats").document(request.getEventId()).set(chats, SetOptions.merge());
            }
        });

        //store join message in event_msg
        eventMsgRef = FirebaseDatabase.getInstance().getReference().child("events_msg").child(request.getEventId());
        String messageKey = eventMsgRef.push().getKey();
        String message = request.getRequestorName() + " has joined the event!";

        Calendar callDate = Calendar.getInstance();
        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM yyyy");
        currentDate = currentDateFormat.format(callDate.getTime());

        Calendar callTime = Calendar.getInstance();
        SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
        currentTime = currentTimeFormat.format(callTime.getTime());

        HashMap<String, Object> groupMessageKey = new HashMap<>();
        eventMsgRef.updateChildren(groupMessageKey);
        EventMsgKeyRef = eventMsgRef.child(messageKey);

        HashMap<String, Object> messageInfoMap = new HashMap<>();
        messageInfoMap.put("senderID", request.getRequestorUid());
        messageInfoMap.put("senderName", request.getRequestorName());
        messageInfoMap.put("senderURL", request.getRequestorAvatar());
        messageInfoMap.put("msgText", message);
        messageInfoMap.put("msgDate", currentDate);
        messageInfoMap.put("msgTime", currentTime);
        messageInfoMap.put("msgType", "join");
        EventMsgKeyRef.updateChildren(messageInfoMap);
        sendNotification(request.getRequestorUid(), request.getEventId(), "Request Accepted", "You joined "+request.getEventTitle());
        db.collection("requests").document(request.getRequestorUid()+"-"+request.getEventId()).delete();
    }

    private void sendNotification(final String receiver, final String eventId, final String title, final  String body){
        Log.d(TAG, "sendNotification: receiver: "+receiver+", eventId: "+eventId);
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("tokens");
        com.google.firebase.database.Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data("EVENT", eventId, R.drawable.ic_plane_24dp, body, title,
                            receiver);
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new retrofit2.Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Log.d(TAG, "response.code: "+response.code());
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
