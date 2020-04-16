package com.hku.tripals;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Place;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class EditEventActivity extends AppCompatActivity {

    private static final String TAG = "CreateEventActivity";
    private static int LOCATION_PICKER_CODE = 1111;
    private static int IMAGE_PICKER_CODE = 2222;
    private Intent intent;

    private ImageButton back;
    private ImageView eventPhoto;
    private Uri eventPhotoUri;
    private EditText eventName;
    private EditText eventDescription;
    private EditText eventDateTime;
    private RadioGroup eventPrivacy;
    private EditText eventQuota;
    private RadioGroup eventOpenness;
    private EditText eventLocation;
    private EditText eventInterests;
    private String[] listItems; //all interest options
    private boolean[] checkedItems; //check boxes
    private ArrayList<Integer> eventItems = new ArrayList<>(); //show checked items
    private List<String> selectedInterest;
    private Button createEvent;
    private ProgressBar progressBar;

    private Event event;

    //variables for event edit
    private String event_id;
    private String event_name;
    private String event_description;
    private String event_photo;
    private String event_datetime;
    private String event_locationName;
    private String event_location;
    private String event_privacy;
    private ArrayList<String> event_interests;
    private String event_openness;
    private Integer event_quota;
    private String itemList = "";

    private FirebaseFirestore db;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        db = FirebaseFirestore.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        event = new Event();
        back = findViewById(R.id.create_event_back_imageButton);

        //getting information to edit event
        event_id = getIntent().getExtras().get("event_id").toString();
        event_name = getIntent().getExtras().get("event_name").toString();
        event_description = getIntent().getStringExtra("event_description");
        event_photo = getIntent().getStringExtra("event_photo");
        event_datetime = getIntent().getStringExtra("event_datetime");
        event_locationName = getIntent().getStringExtra("event_locationName");
        event_location = getIntent().getStringExtra("event_location");
        event_privacy = getIntent().getStringExtra("event_privacy");
        event_interests = getIntent().getStringArrayListExtra("event_interests");
        event_openness = getIntent().getStringExtra("event_openness");
        event_quota = getIntent().getExtras().getInt("event_quota");

        eventPhoto = findViewById(R.id.event_header_imageView);
        eventName = findViewById(R.id.event_name_editText);
        eventDescription = findViewById(R.id.event_desciption_editText);
        eventDateTime = findViewById(R.id.event_date_time_editText);
        eventLocation = findViewById(R.id.event_location_editText);
        eventPrivacy = findViewById(R.id.event_privacy_radioGroup);
        eventInterests = findViewById(R.id.event_interests_select_editText);
        listItems = getResources().getStringArray(R.array.interest_options);
        checkedItems = new boolean[listItems.length];
        createEvent = findViewById(R.id.create_event_button);
        progressBar = findViewById(R.id.create_event_progressBar);
        eventQuota = findViewById(R.id.event_quota_editText);
        eventOpenness = findViewById(R.id.event_openness_radioGroup);
        progressBar.setVisibility(View.GONE);
        eventLocation.setInputType(InputType.TYPE_NULL);
        eventDateTime.setInputType(InputType.TYPE_NULL);
        eventInterests.setInputType(InputType.TYPE_NULL);
        eventDateTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimeDialog();
            }
        });
        intent = getIntent();

        //initializing to display edit event information (can put into a function)
        eventName.setText(event_name);
        eventDescription.setText(event_description);
        Picasso.get().load(event_photo).into(eventPhoto);
        eventDateTime.setText(event_datetime);
        eventLocation.setText(event_locationName);
        if(event_privacy.matches("PUBLIC")){
            eventPrivacy.clearCheck();
            eventPrivacy.check(R.id.event_privacy_radioButton1);
        } else {
            eventPrivacy.clearCheck();
            eventPrivacy.check(R.id.event_privacy_radioButton2);
        }
        for(int i=0; i< event_interests.size();i++){
            itemList = itemList + event_interests.get(i);
            if (i+1 != event_interests.size()){
                itemList = itemList + ", ";
            }
        }
        eventInterests.setText(itemList);
        if(event_openness.matches("OPEN")){
            eventOpenness.clearCheck();
            eventOpenness.check(R.id.event_openness_radioButton1);
        } else {
            eventOpenness.clearCheck();
            eventOpenness.check(R.id.event_openness_radioButton2);
        }
        //eventQuota.setText(event_quota.toString()); (do think it needs to be displayed)

        if(!TextUtils.isEmpty(intent.getStringExtra("place_id"))){
            event.setLocation(intent.getStringExtra("place_id"));
            eventLocation.setText(intent.getStringExtra("place_name"));
            event.setLocationName(intent.getStringExtra("place_name"));
            Bitmap bmp = null;
            try {
                File cachedPhoto = new File(getCacheDir(), intent.getStringExtra("place_id")+".png");
                FileInputStream is = new FileInputStream(cachedPhoto);
                bmp = BitmapFactory.decodeStream(is);
                eventPhoto.setImageBitmap(bmp);
                eventPhotoUri = null;
                is.close();
            } catch (Exception e) {
                Log.d(TAG, "No cached photo");
            }
        }
        eventPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: select avatar clicked");
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_PICKER_CODE);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        });
        eventLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent picker = new Intent(EditEventActivity.this, MapsActivity.class);
                picker.putExtra("type", "location-picker");
                startActivityForResult(picker, LOCATION_PICKER_CODE);
            }
        });
        eventOpenness.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.event_openness_radioButton1){
                    eventQuota.setEnabled(true);
                }else if(i == R.id.event_openness_radioButton2){
                    eventQuota.setText("");
                    eventQuota.setEnabled(false);
                }
            }
        });
        createEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(eventName.getText().toString().matches("") || eventDescription.getText().toString().matches("")
                    || eventDateTime.getText().toString().matches("") || eventLocation.getText().toString().matches("")){
                    Toast.makeText(EditEventActivity.this,
                            R.string.not_complete_event_message, Toast.LENGTH_SHORT).show();
                    if(eventName.getText().toString().matches(""))
                        eventName.setError(getText(R.string.required));
                    if(eventDescription.getText().toString().matches(""))
                        eventDescription.setError(getText(R.string.required));
                    if(eventDateTime.getText().toString().matches(""))
                        eventDateTime.setError(getText(R.string.required));
                    if(eventLocation.getText().toString().matches(""))
                        eventLocation.setError(getText(R.string.required));
                }else{
                    progressBar.setVisibility(View.VISIBLE);
                    event.setHost(currentUser.getUid());
                    event.setTitle(eventName.getText().toString());
                    event.setInterests(selectedInterest);
                    event.setDescription(eventDescription.getText().toString());
                    event.setHostAvatarUrl(currentUser.getPhotoUrl().toString());
                    event.setHostName(currentUser.getDisplayName());
                    if(eventPrivacy.getCheckedRadioButtonId() == R.id.event_privacy_radioButton1){
                        event.setPrivacy("PUBLIC");
                    }else{
                        event.setPrivacy("PRIVATE");
                    }
                    if(eventOpenness.getCheckedRadioButtonId() == R.id.event_openness_radioButton1){
                        event.setOpenness("OPEN");
                        if(!eventQuota.getText().toString().matches("")){
                            event.setQuota(Integer.parseInt(eventQuota.getText().toString()));
                        }else{
                            event.setQuota(-1);
                        }
                    }else{
                        event.setOpenness("CLOSED");
                    }
                    eventPhoto.setEnabled(false);
                    eventName.setEnabled(false);
                    eventDescription.setEnabled(false);
                    eventDateTime.setEnabled(false);
                    eventPrivacy.setEnabled(false);
                    eventOpenness.setEnabled(false);
                    eventQuota.setEnabled(false);
                    eventLocation.setEnabled(false);
                    eventInterests.setEnabled(false);
                    editEvent();
                }
            }
        });
        eventInterests.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog.Builder Interest_Builder = new AlertDialog.Builder(EditEventActivity.this);
                Interest_Builder.setTitle("Select Interest");
                Interest_Builder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                        if (isChecked) {
                            if (!eventItems.contains(position)) { //***Explanation Remarks***
                                eventItems.add(position);
                            }
                        } else {
                            for (int i = 0; i < eventItems.size(); i++)
                                if (eventItems.get(i) == position) {
                                    eventItems.remove(i);
                                    break;
                                }
                        }
                    }
                });
                Interest_Builder.setCancelable(false);
                Interest_Builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String item = "";
                        selectedInterest = new ArrayList<>();
                        for (int i=0; i<eventItems.size(); i++){
                            item = item + listItems[eventItems.get(i)];
                            selectedInterest.add(listItems[eventItems.get(i)]);
                            if(i != eventItems.size() -1) {
                                item = item + ", ";
                            }
                        }
                        eventInterests.setText(item);
                        String check_text = eventInterests.getText().toString();
                        if (check_text.matches("")){
                            eventInterests.setText(itemList);
                            selectedInterest = event_interests;
                        }
                    }
                });
                Interest_Builder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                Interest_Builder.setNeutralButton("Clear all", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i=0; i<checkedItems.length; i++) {
                            checkedItems[i] = false;
                            eventItems.clear();
                            eventInterests.setText("");
                        }
                        String check_text = eventInterests.getText().toString();
                        if (check_text.matches("")){
                            eventInterests.setText(itemList);
                        }
                    }
                });
                AlertDialog interestDialog = Interest_Builder.create();
                interestDialog.show();
            }
        });
    }

    private void showDateTimeDialog() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calendar.set(Calendar.MINUTE,minute);
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        eventDateTime.setText(simpleDateFormat.format(calendar.getTime()));
                        event.setDatetime(calendar.getTime());
                    }
                };
                new TimePickerDialog(EditEventActivity.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false).show();
            }
        };
        new DatePickerDialog(EditEventActivity.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: Result code" + resultCode);
        if(requestCode == LOCATION_PICKER_CODE && resultCode == RESULT_OK) {
            Place selectedPlace = (Place) data.getSerializableExtra("place");
            eventLocation.setText(selectedPlace.getName());
            event.setLocation(selectedPlace.getPlaceId());
            event.setLocationName(selectedPlace.getName());
            event.setPhotoUrl(selectedPlace.getPlaceId());
            Bitmap bmp = null;
            try {
                File cachedPhoto = new File(getCacheDir(), selectedPlace.getPlaceId()+".png");
                FileInputStream is = new FileInputStream(cachedPhoto);
                bmp = BitmapFactory.decodeStream(is);
                eventPhoto.setImageBitmap(bmp);
                eventPhotoUri = null;
                is.close();
            } catch (Exception e) {
                Log.d(TAG, "No cached photo");
            }
        }
        if(requestCode == IMAGE_PICKER_CODE && resultCode == RESULT_OK && data != null){
            Log.d(TAG, "Photo selected");
            eventPhotoUri = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), eventPhotoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            eventPhoto.setImageBitmap(bitmap);
        }
    }

    private void editEvent(){
        Log.d(TAG, "editEvent: called");
        final DocumentReference Editeventref = db.collection("events").document(event_id);
        String check_location = eventLocation.getText().toString();
        if(eventPhotoUri == null && check_location.matches(event_locationName)){
            event.setPhotoUrl(event_photo);
            Editeventref.update("photoUrl", event.getPhotoUrl());
        } else if(eventPhotoUri == null && !(check_location.matches(event_locationName))){
            Bitmap bmp = null;
            try {
                File cachePhoto = new File(getCacheDir(), event.getLocation()+".png");
                FileInputStream is = new FileInputStream(cachePhoto);
                bmp = BitmapFactory.decodeStream(is);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] data = baos.toByteArray();
                final StorageReference ref = FirebaseStorage.getInstance().getReference("/event-images/"+event_id);
                ref.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                event.setPhotoUrl(uri.toString());
                                Log.d(TAG, "event photo url: "+ uri.toString());
                                Editeventref.update("photoUrl", event.getPhotoUrl());
                            }
                        });
                    }
                });
                is.close();
            } catch (Exception e) {
                Log.d(TAG, "edit event error: "+e.getMessage());
            }
        } else {
            final StorageReference ref = FirebaseStorage.getInstance().getReference("/event-images/"+event_id);
            ref.putFile(eventPhotoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            event.setPhotoUrl(uri.toString());
                            Log.d(TAG, "event photo url: "+uri.toString());
                            Editeventref.update("photoUrl", event.getPhotoUrl());
                        }
                    });
                }
            });
        }
        editChatRoom(event);
        Editeventref.update("title", event.getTitle());
        Editeventref.update("description", event.getDescription());
        //Editeventref.update("photoUrl", event.getPhotoUrl());
        if (!(event.getDatetime() == null)){
            Editeventref.update("datetime", event.getDatetime());
        }
        if(!(event.getInterests() == null)){
            Editeventref.update("interests", event.getInterests());
        }
        if(!(event.getLocation() == null)){
            Editeventref.update("location", event.getLocation());
            Editeventref.update("locationName", event.getLocationName());
        }
        Editeventref.update("openness", event.getOpenness());
        Editeventref.update("privacy", event.getPrivacy());
        Editeventref.update("quota", event.getQuota());
        Toast.makeText(EditEventActivity.this, "Updating event", Toast.LENGTH_SHORT).show();
        clearForm();
        Toast.makeText(EditEventActivity.this, "Event Updated", Toast.LENGTH_SHORT).show();
        //finish();
        setContentView(R.layout.activity_main);
    }

    private void editChatRoom(Event event){
        db.collection("chats").document(event_id).update("eventPhotoUrl", event.getPhotoUrl());
        db.collection("chats").document(event_id).update("eventTitle", event.getTitle());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    private void clearForm(){
        progressBar.setVisibility(View.GONE);
        eventName.setEnabled(true);
        eventName.getText().clear();
        eventDescription.setEnabled(true);
        eventDescription.getText().clear();
        eventDateTime.setEnabled(true);
        eventDateTime.getText().clear();
        eventLocation.setEnabled(true);
        eventLocation.getText().clear();
        eventPrivacy.setEnabled(true);
        eventPrivacy.clearCheck();
        eventPrivacy.check(R.id.event_privacy_radioButton1);
        eventOpenness.setEnabled(true);
        eventOpenness.clearCheck();
        eventOpenness.check(R.id.event_openness_radioButton1);
        eventQuota.setEnabled(true);
        eventQuota.getText().clear();
        eventInterests.setEnabled(true);
        eventInterests.getText().clear();
        eventItems = new ArrayList<>();
        checkedItems = new boolean[listItems.length];
        eventPhoto.setImageResource(R.color.colorPrimary);
    }
}
