package com.hku.tripals;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hku.tripals.NotificationService.APIService;
import com.hku.tripals.NotificationService.Client;
import com.hku.tripals.NotificationService.Data;
import com.hku.tripals.NotificationService.Response;
import com.hku.tripals.NotificationService.Sender;
import com.hku.tripals.NotificationService.Token;
import com.hku.tripals.adapter.CommentsAdapter;
import com.hku.tripals.adapter.EventAdapter;
import com.hku.tripals.model.Comment;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Place;
import com.hku.tripals.model.Request;
import com.hku.tripals.model.User;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class EventActivity extends AppCompatActivity {

    private static final String TAG = "EventActivity";
    private static final String BOOKMARK_PREF = "BOOKMARK_PREF";
    private static int IMAGE_PICKER_CODE = 2222;
    private static int EVENT_EDIT_CODE = 3333;
    private static final int SHARE = 1111;
    private Event event;

    private TextView eventLocation;
    private TextView eventDatetime;
    private TextView eventDescription;
    private TextView eventQuotaTitle;
    private TextView eventQuota;
    private TextView eventHostName;
    private TextView eventNoParticipant;
    private ImageView eventHostAvatar;

    private Button eventButton;
    private ImageView appbarBg;
    private AppBarLayout appbar;
    private CircleImageView bookmarkButton;
    private boolean bookmarked = false;
    SharedPreferences bookmarkPref;
    private List<String> bookmarkList = new ArrayList<>();
    private String bookmarkJson;
    private ImageButton shareButton;
    private Bitmap eventBitmap;

    private ArrayList<Comment> comments = new ArrayList<>();
    private ImageView commentUserAvatar;
    private TextView commentUsername;
    private EditText commentText;
    private ImageView commentPhoto;
    private Bitmap commentPhotoBitmap;
    private Button commentUploadImage;
    private Button commentPost;
    private RecyclerView commentView;
    private LinearLayoutManager layoutManager;
    private CommentsAdapter commentsAdapter;
    private Uri commentPhotoUri;

    private FirebaseFirestore db;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private APIService apiService;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);
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
        eventHostName = findViewById(R.id.event_host_textView);
        eventHostAvatar = findViewById(R.id.event_host_avatar_imageView);
        commentUserAvatar = findViewById(R.id.c_avatar_imageView);
        commentUsername = findViewById(R.id.c_username_textView);
        commentText = findViewById(R.id.user_comment_editText);
        commentPost = findViewById(R.id.user_comment_post_button);
        commentView = findViewById(R.id.event_comment_recycler_view);
        bookmarkButton = findViewById(R.id.bookmark_imageView);
        commentPhoto = findViewById(R.id.c_comment_imageView);
        eventNoParticipant = findViewById(R.id.event_number_participant_textView);
        commentUploadImage = findViewById(R.id.c_add_image_button);
        shareButton = findViewById(R.id.event_share_imageButton);
        bookmarkPref = getSharedPreferences(BOOKMARK_PREF, MODE_PRIVATE);
        bookmarkJson = bookmarkPref.getString("bookmark", "[]");
        eventHostName.setText(event.getHostName());
        Glide.with(this).load(event.getHostAvatarUrl()).apply(RequestOptions.circleCropTransform()).into(eventHostAvatar);
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(bookmarkJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                bookmarkList.add(jsonArray.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        bookmarked = bookmarkList.contains(event.getId());
        if(bookmarked){
            bookmarkButton.setImageResource(R.mipmap.ic_bookmark_on);
        }else{
            bookmarkButton.setImageResource(R.mipmap.ic_bookmark_off);
        }
        bookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = bookmarkPref.edit();
                if(bookmarked){
                    bookmarkButton.setImageResource(R.mipmap.ic_bookmark_off);
                    bookmarked = false;
                    bookmarkList.remove(event.getId());
                }else{
                    bookmarkButton.setImageResource(R.mipmap.ic_bookmark_on);
                    bookmarked = true;
                    bookmarkList.add(event.getId());
                }
                JSONArray json = new JSONArray(bookmarkList);
                editor.putString("bookmark", json.toString());
                Log.d(TAG, "bookmark in json: "+json.toString());
                editor.commit();
            }
        });
        commentView.setNestedScrollingEnabled(false);
        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        commentView.setLayoutManager(layoutManager);
        commentsAdapter = new CommentsAdapter(this);
        commentView.setAdapter(commentsAdapter);

        Glide.with(this)
                .asBitmap()
                .load(event.getPhotoUrl())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        appbarBg.setImageBitmap(resource);
                        eventBitmap = resource;
                    }
                }
        );
        if(event.getQuota() == -1){
            eventQuota.setVisibility(View.GONE);
            eventQuotaTitle.setVisibility(View.GONE);
        }else{
            int noPanticipant = 0;
            if(event.getParticipants() != null)
                noPanticipant = event.getParticipants().size();
            String quotaLeft = String.valueOf(event.getQuota()-noPanticipant);
            eventQuota.setText(quotaLeft+" "+getString(R.string.left));
            if(event.getQuota()-noPanticipant == 0){
                eventButton.setEnabled(false);
                eventButton.setText(getString(R.string.full_event));
            }
        }
        if(event.getParticipants() != null) {
            eventNoParticipant.setText(String.valueOf(event.getParticipants().size()));
        }else{
            eventNoParticipant.setText("0");
        }
        if(event.getHost().matches(currentUser.getUid())){
            eventButton.setText(getString(R.string.edit_event));
        }else{
            eventButton.setText(getString(R.string.join_event));
            if(event.getParticipants() != null){
                if(event.getParticipants().contains(currentUser.getUid())){
                    eventButton.setEnabled(false);
                    eventButton.setText(getString(R.string.event_joined));
                }
                if(event.getOpenness().matches("CLOSED")){
                    eventButton.setEnabled(false);
                    eventButton.setText(getString(R.string.private_event));
                }
            }
            if(event.getDatetime().before(new Date())){
                eventButton.setEnabled(false);
                eventButton.setText(getString(R.string.closed_event));
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
                                    eventButton.setText(getString(R.string.request_sent));
                                }
                            } else {
                                Log.d(TAG, "Failed with: ", task.getException());
                            }
                        }
                    });
        }
        Glide.with(this).load(currentUser.getPhotoUrl()).apply(RequestOptions.circleCropTransform()).into(commentUserAvatar);
        Log.d(TAG, "onCreate: user displayname "+currentUser.getDisplayName());
        commentUsername.setText(currentUser.getDisplayName());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(event.getTitle());
        eventLocation.setText(event.getLocationName());
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
                    goToEditEvent();

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
        if(commentPhotoUri == null){
            commentPhoto.setVisibility(View.GONE);
        }
        commentUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(commentPhotoUri == null){
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, IMAGE_PICKER_CODE);
                }else{
                    commentPhotoUri = null;
                    commentPhoto.setVisibility(View.GONE);
                    commentUploadImage.setText(R.string.add_image);
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
                    commentPhoto.setVisibility(View.GONE);
                    commentPhotoUri = null;
                    commentUploadImage.setText(R.string.add_image);
                    commentText.setEnabled(true);
                    commentPost.setEnabled(true);
                }
            }
        });
        getCommentData(event.getId());
        eventHostName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!event.getHost().matches(currentUser.getUid())) {
                    eventHostName.setEnabled(false);
                    eventHostAvatar.setEnabled(false);
                    goToUser(event.getHost());
                    eventHostName.setEnabled(true);
                    eventHostAvatar.setEnabled(true);
                }
            }
        });
        eventHostAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!event.getHost().matches(currentUser.getUid())) {
                    eventHostName.setEnabled(false);
                    eventHostAvatar.setEnabled(false);
                    goToUser(event.getHost());
                    eventHostName.setEnabled(true);
                    eventHostAvatar.setEnabled(true);
                }
            }
        });
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (ActivityCompat.checkSelfPermission(EventActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(EventActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, SHARE);
                    } else {
                        shareTo();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
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
                sendNotification(event.getHost(), event.getId(), "New Request from "+currentUser.getDisplayName(), currentUser.getDisplayName()+" wants to join "+event.getTitle());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error adding request", e);
            }
        });
        eventButton.setEnabled(false);
    }

    private void postComment(final String commentString){
        Log.d(TAG, "postComment: called");
        final String eventKey = event.getId();
        final DocumentReference newCommentRef = db.collection("events").document(eventKey).collection("comments").document();
        String newCommentId = newCommentRef.getId();
        final Comment comment = new Comment(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getPhotoUrl().toString(), commentString, event.getHost(), "NO", event.getId(), newCommentId);
        if(commentPhotoUri == null){
            newCommentRef.set(comment.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess: comment added");
                    if(!event.getHost().matches(currentUser.getUid())){
                        sendNotification(event.getHost(), eventKey, "New Comment from "+currentUser.getDisplayName(), currentUser.getDisplayName()+": "+commentString);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error adding document", e);
                }
            });
        }else{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            commentPhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final StorageReference ref = FirebaseStorage.getInstance().getReference("/comment-images/"+newCommentId);
            ref.putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.d(TAG, "avatar photo uploaded");
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            comment.setCommentPhoto(uri.toString());
                            Log.d(TAG, "comment photo url: "+uri.toString());
                            newCommentRef.set(comment.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: comment added");
                                    if(!event.getHost().matches(currentUser.getUid())){
                                        sendNotification(event.getHost(), eventKey, "New Comment from "+currentUser.getDisplayName(), currentUser.getDisplayName()+": "+commentString);
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                }
                            });
                        }
                    });
                }
            });
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: Result code" + resultCode);
        if(requestCode == IMAGE_PICKER_CODE && resultCode == RESULT_OK && data != null){
            Log.d(TAG, "Photo selected");
            commentPhotoUri = data.getData();
            try {
                commentPhotoBitmap = decodeUri(this, commentPhotoUri, 1080);
            } catch (IOException e) {
                e.printStackTrace();
            }
            commentPhoto.setImageBitmap(commentPhotoBitmap);
            commentPhoto.setVisibility(View.VISIBLE);
            commentUploadImage.setText(R.string.remove_image);
        }
        if(requestCode == EVENT_EDIT_CODE && resultCode == RESULT_OK){
            final Event updated = (Event) data.getSerializableExtra("UPDATED_EVENT");
            Picasso
                    .get()
                    .load(updated.getPhotoUrl())
                    .fetch(new Callback(){
                        @Override
                        public void onSuccess() {
                            Picasso
                                    .get()
                                    .load(updated.getPhotoUrl())
                                    .into(appbarBg);
                        }
                        @Override
                        public void onError(Exception e) { }
                    });
            if(updated.getQuota() == -1){
                eventQuota.setVisibility(View.GONE);
                eventQuotaTitle.setVisibility(View.GONE);
            }else{
                int noPanticipant = 0;
                if(updated.getParticipants() != null)
                    noPanticipant = updated.getParticipants().size();
                String quotaLeft = String.valueOf(updated.getQuota()-noPanticipant);
                eventQuota.setText(quotaLeft+" "+getString(R.string.left));
            }
            eventLocation.setText(updated.getLocationName());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            eventDatetime.setText(simpleDateFormat.format(updated.getDatetime()));
            eventDescription.setText(updated.getDescription());
        }
    }

    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while(true) {
            if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }

    private void goToUser(String uid){
        db.collection("user-profile").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                User user = documentSnapshot.toObject(User.class);
                Log.d(TAG, "onClick: go user detail :" + user.getUid());
                Intent myIntent = new Intent(EventActivity.this, UserProfileActivity.class);
                myIntent.putExtra("user", (Serializable) user);
                startActivity(myIntent);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    private void goToEditEvent(){
        Intent editIntent = new Intent(EventActivity.this, EditEventActivity.class);
        editIntent.putExtra("event", (Serializable) event);
        startActivityForResult(editIntent, EVENT_EDIT_CODE);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void onResume() {
        super.onResume();
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

    private void shareTo() {
        String shareBody = event.getTitle()+" at "+simpleDateFormat.format(event.getDatetime())+" hosted by "+event.getHostName()+"\nDescription: "+event.getDescription();
//        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//        sharingIntent.setType("text/plain");
//        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Event in Tripals: "+event.getTitle());
//        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
//        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, getImageUri(eventBitmap));
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Event in Tripals: "+event.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
//        shareIntent.setPackage("com.instagram.android");
//        startActivity(shareIntent);

        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_using)));
    }

    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(EventActivity.this.getContentResolver(), inImage, UUID.randomUUID().toString() + ".png", "drawing");
        return Uri.parse(path);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case SHARE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    shareTo();
                } else {
                    Toast.makeText(this, "Please granted storage access right to share", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
