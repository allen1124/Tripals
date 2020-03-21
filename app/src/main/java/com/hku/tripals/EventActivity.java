package com.hku.tripals;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hku.tripals.adapter.CommentsAdapter;
import com.hku.tripals.adapter.EventAdapter;
import com.hku.tripals.model.Comment;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Place;
import com.hku.tripals.model.Request;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventActivity extends AppCompatActivity {

    private static final String TAG = "EventActivity";
    private Event event;

    private TextView eventLocation;
    private TextView eventDatetime;
    private TextView eventDescription;
    private TextView eventQuotaTitle;
    private TextView eventQuota;
    private Button eventButton;
    private ImageView appbarBg;
    private AppBarLayout appbar;

    private ArrayList<Comment> comments = new ArrayList<>();
    private ImageView commentUserAvatar;
    private TextView commentUsername;
    private EditText commentText;
    private Button commentPost;
    private RecyclerView commentView;
    private LinearLayoutManager layoutManager;
    private CommentsAdapter commentsAdapter;

    private FirebaseFirestore db;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        db = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Intent intent = getIntent();
        appbar = (AppBarLayout) findViewById(R.id.app_bar);
        appbarBg = findViewById(R.id.event_appbar_imageView);
        event = (Event) intent.getSerializableExtra("event");
        eventLocation = findViewById(R.id.event_location_textView);
        eventDatetime = findViewById(R.id.event_date_time_textView);
        eventDescription = findViewById(R.id.event_description_textView);
        eventQuotaTitle = findViewById(R.id.event_quota_title_textView);
        eventQuota = findViewById(R.id.event_quota_textView);
        eventButton = findViewById(R.id.event_join_edit_button);
        commentUserAvatar = findViewById(R.id.c_avatar_imageView);
        commentUsername = findViewById(R.id.c_username_textView);
        commentText = findViewById(R.id.user_comment_editText);
        commentPost = findViewById(R.id.user_comment_post_button);
        commentView = findViewById(R.id.event_comment_recycler_view);
        commentView.setNestedScrollingEnabled(false);
        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        commentView.setLayoutManager(layoutManager);
        commentsAdapter = new CommentsAdapter(this);
        commentView.setAdapter(commentsAdapter);
        if(event.getLocation() != null){
            Bitmap bmp = null;
            try {
                File cachedPhoto = new File(getCacheDir(), event.getLocation()+".png");
                FileInputStream is = new FileInputStream(cachedPhoto);
                bmp = BitmapFactory.decodeStream(is);
                appbarBg.setBackground(new BitmapDrawable(this.getResources(), bmp));
                is.close();
            } catch (Exception e) {
                Log.d(TAG, "No cached photo");
                Glide.with(this)
                        .load(event.getPhotoUrl())
                        .into(appbarBg);
            }
        }
        if(event.getQuota() == -1){
            eventQuota.setVisibility(View.GONE);
            eventQuotaTitle.setVisibility(View.GONE);
        }else{
            int noPanticipant = 0;
            if(event.getParticipants() != null)
                noPanticipant = event.getParticipants().size();
            String quotaLeft = String.valueOf(event.getQuota()-noPanticipant);
            eventQuota.setText(quotaLeft+" "+getString(R.string.left));
        }
        if(event.getHost().matches(currentUser.getUid())){
            eventButton.setText(getString(R.string.edit_event));
        }else{
            eventButton.setText(getString(R.string.join_event));
            if(event.getParticipants() != null){
                if(event.getParticipants().contains(currentUser.getUid()) || event.getOpenness().matches("CLOSED")){
                    eventButton.setEnabled(false);
                }
            }
            db.collection("requests").document(currentUser.getUid()+'-'+event.getId()).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "Request exists!");
                                    eventButton.setEnabled(false);
                                }
                            } else {
                                Log.d(TAG, "Failed with: ", task.getException());
                            }
                        }
                    });
        }
        Glide.with(this).load(currentUser.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(commentUserAvatar);
        commentUsername.setText(currentUser.getDisplayName());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(event.getTitle());
        eventLocation.setText(event.getLocationName());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        eventDatetime.setText(simpleDateFormat.format(event.getDatetime()));
        eventDescription.setText(event.getDescription());
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                try {
                    intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/dir/?api=1&destination="+ URLEncoder.encode(event.getLocationName(), "utf-8")+"&destination_place_id="+event.getLocation()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });
        eventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(event.getHost().matches(currentUser.getUid())) {
                    Snackbar.make(view, "Edit Event", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                }else{
                    new AlertDialog.Builder(EventActivity.this)
                            .setTitle("Confirmation")
                            .setMessage("Do you really want to join this event?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    joinEvent();
                                }})
                            .setNegativeButton(android.R.string.no, null).show();
                }
            }
        });
        commentPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String commentString = commentText.getText().toString();
                if(commentString.matches("")){
                    Toast.makeText(EventActivity.this, R.string.comment_alert, Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    commentText.setEnabled(false);
                    commentPost.setEnabled(false);
                    postComment(commentString);
                    commentText.getText().clear();
                    commentText.setEnabled(true);
                    commentPost.setEnabled(true);
                }
            }
        });
        getCommentData(event.getId());
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

    private void joinEvent(){
        Log.d(TAG, "joinEvent: called");

        DocumentReference requestRef = db.collection("requests").document(currentUser.getUid()+'-'+event.getId());
        int participant;
        if(event.getParticipants() == null)
            participant = 0;
        else
            participant = event.getParticipants().size();
        Request request = new Request(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getPhotoUrl().toString(), event.getId(), event.getTitle(), event.getPhotoUrl(), event.getHost(), event.getQuota(), participant);
        requestRef.set(request.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Request added");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding request", e);
            }
        });
        eventButton.setEnabled(false);
    }

    private void postComment(String commentString){
        Log.d(TAG, "postComment: called");
        String eventKey = event.getId();
        DocumentReference newCommentRef = db.collection("events").document(eventKey).collection("comments").document();
        Comment comment = new Comment(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getPhotoUrl().toString(), commentString);
        newCommentRef.set(comment.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: comment added");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding document", e);
            }
        });
    }

    private void getCommentData(String eventId){
        Log.d(TAG, "getCommentData: called");
        db.collection("events").document(eventId).collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Log.w(TAG, "Getting documents.");
                if (e != null) {
                    Log.w(TAG, "Error getting documents.", e);
                    return;
                }
                comments.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    comments.add(document.toObject(Comment.class));
                    Log.d(TAG, document.getId() + " added");
                }
                commentsAdapter.setCommentList(comments);
                commentsAdapter.notifyDataSetChanged();
            }
        });
    }
}
