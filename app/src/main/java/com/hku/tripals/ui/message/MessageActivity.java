package com.hku.tripals.ui.message;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hku.tripals.R;
import com.hku.tripals.adapter.MessageAdapter;
import com.hku.tripals.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private Toolbar chat_toolbar;
    private Button send_Button;
    private FloatingActionButton scrollDown;
    private EditText userInput;
    private RecyclerView msg;
    private NestedScrollView mView2;
    private String current_event_name, current_event_id, current_event_image;
    private String currentUserID, currentUserName;
    private String currentDate, currentTime;

    private List<Message> msgList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, EventRef, EventMsgKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //get current user
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        current_event_id = getIntent().getExtras().get("eventID").toString();
        current_event_name = getIntent().getExtras().get("eventName").toString();

        //UsersRef = FirebaseDatabase.getInstance().getReference().child("users_profile");
        EventRef = FirebaseDatabase.getInstance().getReference().child("events_msg").child(current_event_id);

        chat_toolbar = (Toolbar) findViewById(R.id.Chatlog_toolbar);
        setSupportActionBar(chat_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle((current_event_name));

        send_Button = (Button) findViewById(R.id.send_button);
        scrollDown = (FloatingActionButton) findViewById(R.id.floatingActionButton_scrollDown);
        userInput = (EditText) findViewById(R.id.sendmsg_editText);
        msg = (RecyclerView) findViewById(R.id.chatlog_recycler);
        mView2 = (NestedScrollView) findViewById(R.id.scrollView4);

       messageAdapter = new MessageAdapter(MessageActivity.this,msgList);
       linearLayoutManager = new LinearLayoutManager(this);
       msg.setLayoutManager(linearLayoutManager);
       msg.setAdapter(messageAdapter);

        EventRef.child(current_event_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    current_event_name = dataSnapshot.child("event_name").getValue().toString();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        scrollDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mView2.fullScroll(NestedScrollView.FOCUS_DOWN);
            }
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
                        messageInfoMap.put("msgText", message);
                        messageInfoMap.put("msgDate", currentDate);
                        messageInfoMap.put("msgTime", currentTime);
                    EventMsgKeyRef.updateChildren(messageInfoMap);
                }
                userInput.setText("");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    Message message = dataSnapshot.getValue(Message.class);
                    msgList.add(message);
                    messageAdapter.notifyDataSetChanged();
                    //msg.smoothScrollToPosition(msg.getAdapter().getItemCount());
                }
                mView2.fullScroll(NestedScrollView.FOCUS_DOWN);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    Message message = dataSnapshot.getValue(Message.class);
                    msgList.add(message);
                    messageAdapter.notifyDataSetChanged();
                }
                mView2.fullScroll(NestedScrollView.FOCUS_DOWN);
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }
}
