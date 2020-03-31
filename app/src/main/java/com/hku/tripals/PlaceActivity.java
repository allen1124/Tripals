package com.hku.tripals;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hku.tripals.adapter.EventAdapter;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Place;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PlaceActivity extends AppCompatActivity {

    private static final String TAG = "PlaceActivity";
    private Place place;
    private AppBarLayout appbar;
    private FloatingActionButton fabButton;
    private RecyclerView event;
    private LinearLayoutManager eventLayoutManager;
    private EventAdapter eventAdapter;
    private List<Event> eventList = new ArrayList<>();
    private TextView noEvent;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        Intent intent = getIntent();
        appbar = (AppBarLayout) findViewById(R.id.app_bar);
        place = (Place) intent.getSerializableExtra("place");
        noEvent = findViewById(R.id.no_event_textView);
        noEvent.setVisibility(View.GONE);
        if(intent.getStringExtra("photo") != null){
            Bitmap bmp = null;
            try {
                File cachedPhoto = new File(getCacheDir(), intent.getStringExtra("photo")+".png");
                FileInputStream is = new FileInputStream(cachedPhoto);
                bmp = BitmapFactory.decodeStream(is);
                appbar.setBackground(new BitmapDrawable(this.getResources(), addGradient(bmp)));
                is.close();
            } catch (Exception e) {
                Log.d(TAG, "No cached photo");
            }
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        event = findViewById(R.id.place_event_recyclerView);
        eventLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        event.setLayoutManager(eventLayoutManager);
        eventAdapter = new EventAdapter(this);
        event.setAdapter(eventAdapter);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(place.getName());
        fabButton = (FloatingActionButton) findViewById(R.id.fab);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PlaceActivity.this, CreateEventActivity.class);
                intent.putExtra("place_id", place.getPlaceId());
                intent.putExtra("place_name", place.getName());
                startActivity(intent);
            }
        });
        loadEvent(10);
    }

    private void loadEvent(int number){
        Log.d(TAG, "loadEvent: called");
        db.collection("events")
                .whereEqualTo("privacy", "PUBLIC")
                .whereEqualTo("location", place.getPlaceId())
                .limit(number)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        Log.w(TAG, "Getting documents.");
                        if (e != null) {
                            Log.w(TAG, "Error getting documents.", e);
                            return;
                        }
                        eventList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            eventList.add(document.toObject(Event.class));
                            Log.d(TAG, document.getId() + " added");
                        }
                        if(eventList.size() == 0){
                            noEvent.setVisibility(View.VISIBLE);
                        }
                        eventAdapter.setEventList(eventList);
                        eventAdapter.notifyDataSetChanged();
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

    public Bitmap addGradient(Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        Bitmap overlay = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.drawBitmap(src, 0, 0, null);
        Paint paint = new Paint();
        LinearGradient shader = new LinearGradient(0, h-200, 0, h, 0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, h-200, w, h, paint);
        return overlay;
    }
}
