package com.hku.tripals.ui.message;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hku.tripals.NotificationService.APIService;
import com.hku.tripals.NotificationService.Client;
import com.hku.tripals.NotificationService.Data;
import com.hku.tripals.NotificationService.Response;
import com.hku.tripals.NotificationService.Sender;
import com.hku.tripals.NotificationService.Token;
import com.hku.tripals.R;
import com.hku.tripals.UserProfileActivity;
import com.hku.tripals.adapter.MessageAdapter;
import com.hku.tripals.model.Message;
import com.hku.tripals.model.User;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private static final String TAG = "MessageActivity";
    private Toolbar chat_toolbar;
    private Button send_Button;
    private ImageButton sendimg_Button;
    private EditText userInput;
    private RecyclerView msg;
    private String chat_name, chat_id, chat_icon, type, participants, targetUID;
    private String currentUserID, currentUserName, currentUserURL;
    private String currentDate, currentTime;
    private String checker = "", theUrl = "";
    private CircleImageView chatIcon;
    private TextView chatTitle;
    private Uri fileUri;
    private StorageTask uploadTask;

    private List<Message> msgList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference EventRef, EventMsgKeyRef;
    private FirebaseFirestore db;
    private String P_UserID;

    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUserID = mAuth.getCurrentUser().getUid();

        chat_id = getIntent().getExtras().get("Chat_Id").toString();
        chat_name = getIntent().getExtras().get("Chat_Name").toString();
        chat_icon = getIntent().getStringExtra("Chat_Icon");
        type = getIntent().getStringExtra("type");
        participants = getIntent().getStringExtra("participants");

        if (type.matches("INDIVIDUAL")){
            targetUID = getIntent().getStringExtra("participants");
        }

        EventRef = FirebaseDatabase.getInstance().getReference().child("events_msg").child(chat_id);
        db = FirebaseFirestore.getInstance();

        currentUserName = currentUser.getDisplayName();
        currentUserURL = currentUser.getPhotoUrl().toString();

        chat_toolbar = (Toolbar) findViewById(R.id.Chatlog_toolbar);
        setSupportActionBar(chat_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");
        chatIcon = findViewById(R.id.chat_icon);
        Glide.with(this)
                .load(chat_icon)
                .into(chatIcon);
        chatTitle = findViewById(R.id.chat_title);
        chatTitle.setText(chat_name);
        chatIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(type.matches("EVENT")){
                    showParticipants(participants);
                }else{
                    if(!targetUID.matches(currentUserID)) {
                        goToUser(targetUID);
                    }
                    Log.d(TAG, "it is a 1-1 chat, go to user profile");
                }
            }
        });
        chatTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(type.matches("EVENT")){
                    showParticipants(participants);
                }else{
                    if(!targetUID.matches(currentUserID)) {
                        goToUser(targetUID);
                    }
                    Log.d(TAG, "it is a 1-1 chat, go to user profile");
                }
            }
        });
        send_Button = (Button) findViewById(R.id.send_button);
        sendimg_Button = (ImageButton) findViewById(R.id.sendFiles_button);
        userInput = (EditText) findViewById(R.id.sendmsg_editText);
        msg = (RecyclerView) findViewById(R.id.chatlog_recycler);

        messageAdapter = new MessageAdapter(MessageActivity.this,msgList);
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        msg.setLayoutManager(linearLayoutManager);
        msg.setAdapter(messageAdapter);

        EventRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    Message message = dataSnapshot.getValue(Message.class);
                    msgList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    msg.smoothScrollToPosition(messageAdapter.getItemCount());
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    Message message = dataSnapshot.getValue(Message.class);
                    msgList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    msg.smoothScrollToPosition(messageAdapter.getItemCount());
                }
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        send_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = userInput.getText().toString();
                String messageKey = EventRef.push().getKey();

                if (TextUtils.isEmpty(message)){
                } else {
                    userInput.setText("");
                    Calendar callDate = Calendar.getInstance();
                    SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM yyyy");
                    currentDate = currentDateFormat.format(callDate.getTime());

                    Calendar callTime = Calendar.getInstance();
                    SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                    currentTime = currentTimeFormat.format(callTime.getTime());

                    HashMap<String, Object> groupMessageKey = new HashMap<>();
                    EventRef.updateChildren(groupMessageKey);
                    EventMsgKeyRef = EventRef.child(messageKey);

                    HashMap<String, Object> messageInfoMap = new HashMap<>();
                        messageInfoMap.put("senderID", currentUserID);
                        messageInfoMap.put("senderName", currentUserName);
                        messageInfoMap.put("senderURL", currentUserURL);
                        messageInfoMap.put("msgText", message);
                        messageInfoMap.put("msgDate", currentDate);
                        messageInfoMap.put("msgTime", currentTime);
                        messageInfoMap.put("msgType", "text");
                    EventMsgKeyRef.updateChildren(messageInfoMap);

                    if(type.matches("EVENT")){
                        participants = participants.replaceAll("^\\[|]$", "");
                        List<String> userList = new ArrayList<String>(Arrays.asList(participants.split(", ")));
                        for (int i = 0; i < userList.size(); i++) {
                            if(currentUserID != userList.get(i)){
                                sendNotification(userList.get(i), chat_id, chat_name, chat_icon, type, participants, currentUser.getDisplayName(), message);
                            }
                        }
                    }else if(type.matches("INDIVIDUAL")){
                        sendNotification( targetUID, chat_id, currentUserName, currentUserURL, type, currentUser.getUid(), currentUser.getDisplayName(), message);
                    }
                }
            }
        });

        sendimg_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Image",
                                //"PDF Files",
                                //"Docs Files"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
                builder.setTitle("Please select");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select a photo"), 438);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode==RESULT_OK && data != null && data.getData() != null){
            fileUri = data.getData();
            if (!checker.equals("image")){
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            } else if (checker.equals("image")){
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("event_msg_image");
                final String messageKey = EventRef.push().getKey();
                final StorageReference filePath = storageReference.child(messageKey);
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri downloadUrI = task.getResult();
                            theUrl = downloadUrI.toString();

                            Calendar callDate = Calendar.getInstance();
                            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM yyyy");
                            currentDate = currentDateFormat.format(callDate.getTime());

                            Calendar callTime = Calendar.getInstance();
                            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                            currentTime = currentTimeFormat.format(callTime.getTime());

                            HashMap<String, Object> groupMessageKey = new HashMap<>();
                            EventRef.updateChildren(groupMessageKey);
                            EventMsgKeyRef = EventRef.child(messageKey);

                            HashMap<String, Object> messageInfoMap = new HashMap<>();
                            messageInfoMap.put("senderID", currentUserID);
                            messageInfoMap.put("senderName", currentUserName);
                            messageInfoMap.put("senderURL", currentUserURL);
                            messageInfoMap.put("msgText", theUrl);
                            messageInfoMap.put("msgDate", currentDate);
                            messageInfoMap.put("msgTime", currentTime);
                            messageInfoMap.put("msgType", "image");
                            EventMsgKeyRef.updateChildren(messageInfoMap);
                        }
                    }
                });
            } else {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        currentUser("none");
        finish();
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                super.onBackPressed();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showParticipants(String participants){
        participants = participants.replaceAll("^\\[|]$", "");
        final List<String> userList = new ArrayList<String>(Arrays.asList(participants.split(", ")));
        Log.d(TAG, "showParticipants: "+userList.toString());
        AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
        builder.setTitle(getString(R.string.participants));
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, R.layout.recyclerview_chats, userList){
            @Override
            public View getView(int position, View view, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.recyclerview_chats, null);
                CardView cardView = view.findViewById(R.id.Chats_cardview);
                cardView.setRadius(0f);
                TextView lastMsg = view.findViewById(R.id.chatpreview_textview);
                final TextView username = view.findViewById(R.id.event_name_textview);
                final CircleImageView avatarIcon = view.findViewById(R.id.circleEventImageView);
                lastMsg.setVisibility(View.GONE);
                DocumentReference userInfo = db.collection("user-profile").document(userList.get(position));
                userInfo.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        username.setText(documentSnapshot.getString("displayName"));
                        Glide.with(getContext()).load(documentSnapshot.getString("avatarImageUrl")).into(avatarIcon);
                    }
                });
                return view;
            }
        };

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int position) {
                Log.d(TAG, "onClick: (Yes) "+userList.get(position));
                if(!(userList.get(position)).matches(currentUserID)) {
                    goToUser(userList.get(position));
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void goToUser(String uid){
        db.collection("user-profile").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                Log.d(TAG, "onClick: go user detail :" + user.getUid());
                Intent myIntent = new Intent(MessageActivity.this, UserProfileActivity.class);
                myIntent.putExtra("user", (Serializable) user);
                startActivity(myIntent);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }


    private void sendNotification(final String receiver, final String chatId, final String chatName, final String chatIcon, final String type, final String participants, final String username, final String message){
        Log.d(TAG, "sendNotification: receiver: "+receiver+", chatId: "+chatId);
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Log.d("Debug", "userid before sent: "+receiver);
                    Data data = new Data("NEWMESSAGE", chatId, chatName, chatIcon, type, participants, R.drawable.ic_plane_24dp, username+": "+message, "New Message from "+username,
                            receiver);
                    Sender sender = new Sender(data, token.getToken());
                    Log.d("Debug", "data is: "+ data);

                    Log.d("Debug", "token is: "+ token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
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

    private void currentUser(String userid){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("CURRENT_USER", userid);
        editor.apply();
    }

    @Override
    public void onResume() {
        super.onResume();
        currentUser(currentUserID);
    }

    @Override
    public void onPause() {
        super.onPause();
        currentUser("none");
    }
}
