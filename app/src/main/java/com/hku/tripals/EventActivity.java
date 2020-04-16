package com.hku.tripals;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventActivity extends AppCompatActivity {

    private static final String TAG = "EventActivity";
    private static final String BOOKMARK_PREF = "BOOKMARK_PREF";
    private static int IMAGE_PICKER_CODE = 2222;
    private Event event;

    private TextView eventLocation;
    private TextView eventDatetime;
    private TextView eventDescription;
    private TextView eventQuotaTitle;
    private TextView eventQuota;
    private TextView eventHostName;
    private ImageView eventHostAvatar;

    private Button eventButton;
    private ImageView appbarBg;
    private AppBarLayout appbar;
    private CircleImageView bookmarkButton;
    private boolean bookmarked = false;
    SharedPreferences bookmarkPref;
    private List<String> bookmarkList = new ArrayList<>();
    private String bookmarkJson;

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
        eventHostName = findViewById(R.id.event_host_textView);
        eventHostAvatar = findViewById(R.id.event_host_avatar_imageView);
        commentUserAvatar = findViewById(R.id.c_avatar_imageView);
        commentUsername = findViewById(R.id.c_username_textView);
        commentText = findViewById(R.id.user_comment_editText);
        commentPost = findViewById(R.id.user_comment_post_button);
        commentView = findViewById(R.id.event_comment_recycler_view);
        bookmarkButton = findViewById(R.id.bookmark_imageView);
        commentPhoto = findViewById(R.id.c_comment_imageView);
        commentUploadImage = findViewById(R.id.c_add_image_button);
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
        if(event.getLocation() != null){
            //Bitmap bmp = null;
            try {
                Picasso
                        .get()
                        .load(event.getPhotoUrl())
                        .fetch(new Callback(){
                            @Override
                            public void onSuccess() {
                                Picasso
                                        .get()
                                        .load(event.getPhotoUrl())
                                        .into(appbarBg);
                            }
                            @Override
                            public void onError(Exception e) { }
                        });
//                File cachedPhoto = new File(getCacheDir(), event.getLocation()+".png");
//                FileInputStream is = new FileInputStream(cachedPhoto);
//                bmp = BitmapFactory.decodeStream(is);
//                appbarBg.setBackground(new BitmapDrawable(this.getResources(), bmp));
//                is.close();
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
        Log.d(TAG, "onCreate: user displayname "+currentUser.getDisplayName());
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
//                    Snackbar.make(view, "Edit Event", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                    goToEditEvent(event.getId());

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
        final DocumentReference newCommentRef = db.collection("events").document(eventKey).collection("comments").document();
        String newCommentId = newCommentRef.getId();
        final Comment comment = new Comment(currentUser.getUid(), currentUser.getDisplayName(), currentUser.getPhotoUrl().toString(), commentString);
        if(commentPhotoUri == null){
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

    private void goToEditEvent(String eventID){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Intent editIntent = new Intent(EventActivity.this, EditEventActivity.class);
        editIntent.putExtra("event_id", eventID);
        editIntent.putExtra("event_name", event.getTitle());
        editIntent.putExtra("event_description", event.getDescription());
        editIntent.putExtra("event_photo", event.getPhotoUrl());
        editIntent.putExtra("event_datetime", simpleDateFormat.format(event.getDatetime()));
        editIntent.putExtra("event_locationName", event.getLocationName());
        editIntent.putExtra("event_location", event.getLocation());
        editIntent.putExtra("event_privacy", event.getPrivacy());
        editIntent.putStringArrayListExtra("event_interests", (ArrayList<String>) event.getInterests());
        editIntent.putExtra("event_openness", event.getOpenness());
        editIntent.putExtra("event_quota", event.getQuota());
        startActivity(editIntent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
}
