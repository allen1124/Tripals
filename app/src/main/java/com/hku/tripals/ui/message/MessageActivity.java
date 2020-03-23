package com.hku.tripals.ui.message;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Tag;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.hku.tripals.R;
import com.hku.tripals.adapter.MessageAdapter;
import com.hku.tripals.model.Message;

import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private Toolbar chat_toolbar;
    private Button send_Button;
    private ImageButton sendimg_Button;
    private EditText userInput;
    private RecyclerView msg;
    private String current_event_name, current_event_id, current_event_image;
    private String currentUserID, currentUserName, currentUserURL;
    private String currentDate, currentTime;
    private String checker ="", theUrl = "";
    private Uri fileUri;
    private StorageTask uploadTask;

    private List<Message> msgList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, EventRef, EventMsgKeyRef;
    private FirebaseFirestore db;
    private DocumentReference docRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        current_event_id = getIntent().getExtras().get("eventID").toString();
        current_event_name = getIntent().getExtras().get("eventName").toString();

        //UsersRef = FirebaseDatabase.getInstance().getReference().child("users_profile");
        EventRef = FirebaseDatabase.getInstance().getReference().child("events_msg").child(current_event_id);
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("user-profile").document(currentUserID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                currentUserName = documentSnapshot.getString("displayName");
                currentUserURL = documentSnapshot.getString("avatarImageUrl");
            }
        });

        chat_toolbar = (Toolbar) findViewById(R.id.Chatlog_toolbar);
        setSupportActionBar(chat_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle((current_event_name));

        send_Button = (Button) findViewById(R.id.send_button);
        sendimg_Button = (ImageButton) findViewById(R.id.sendFiles_button);
        userInput = (EditText) findViewById(R.id.sendmsg_editText);
        msg = (RecyclerView) findViewById(R.id.chatlog_recycler);

       messageAdapter = new MessageAdapter(MessageActivity.this,msgList);
       linearLayoutManager = new LinearLayoutManager(this);
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
                }
                userInput.setText("");
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
}
