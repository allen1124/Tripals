package com.hku.tripals;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.Maps;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hku.tripals.model.Event;
import com.hku.tripals.model.Place;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreateEventActivity extends AppCompatActivity {

    private static final String TAG = "CreateEventActivity";
    private static int LOCATION_PICKER_CODE = 1111;
    private Intent intent;

    private ImageButton back;
    private EditText eventName;
    private EditText eventDescription;
    private EditText eventDateTime;
    private EditText eventLocation;
    private EditText eventInterests;
    private String[] listItems; //all interest options
    private boolean[] checkedItems; //check boxes
    private ArrayList<Integer> eventItems = new ArrayList<>(); //show checked items
    private List<String> selectedInterest;
    private Button createEvent;

    private Event event;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        event = new Event();
        back = findViewById(R.id.create_event_back_imageButton);
        eventName = findViewById(R.id.event_name_editText);
        eventDescription = findViewById(R.id.event_desciption_editText);
        eventDateTime = findViewById(R.id.event_date_time_editText);
        eventLocation = findViewById(R.id.event_location_editText);
        eventInterests = findViewById(R.id.event_interests_select_editText);
        listItems = getResources().getStringArray(R.array.interest_options);
        checkedItems = new boolean[listItems.length];
        createEvent = findViewById(R.id.create_event_button);
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
        if(!TextUtils.isEmpty(intent.getStringExtra("place_id"))){
            event.setLocation(intent.getStringExtra("place_id"));
            eventLocation.setText(intent.getStringExtra("place_name"));
        }
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
                Intent picker = new Intent(CreateEventActivity.this, MapsActivity.class);
                picker.putExtra("type", "location-picker");
                startActivityForResult(picker, LOCATION_PICKER_CODE);
            }
        });
        createEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(eventName.getText().toString().matches("") || eventDescription.getText().toString().matches("")
                    || eventDateTime.getText().toString().matches("") || eventLocation.getText().toString().matches("")){
                    Toast.makeText(CreateEventActivity.this,
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
                    event.setHost(currentUser.getUid());
                    event.setTitle(eventName.getText().toString());
                    event.setInterests(selectedInterest);
                    event.setDescription(eventDescription.getText().toString());
                    eventName.setEnabled(false);
                    eventDescription.setEnabled(false);
                    eventDateTime.setEnabled(false);
                    eventLocation.setEnabled(false);
                    eventInterests.setEnabled(false);
                    addEvent();
                }
            }
        });
        eventInterests.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog.Builder Interest_Builder = new AlertDialog.Builder(CreateEventActivity.this);
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
                new TimePickerDialog(CreateEventActivity.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),false).show();
            }
        };
        new DatePickerDialog(CreateEventActivity.this,dateSetListener,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: Result code" + resultCode);
        if(resultCode == RESULT_OK) {
            Place selectedPlace = (Place) data.getSerializableExtra("place");
            eventLocation.setText(selectedPlace.getName());
            event.setLocation(selectedPlace.getPlaceId());
            event.setLocationPhotoUrl(selectedPlace.getPhotoReference());
        }
    }

    private void addEvent(){
        DocumentReference newEventRef = db.collection("events").document();
        event.setId(newEventRef.getId());
        newEventRef.set(event.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "DocumentSnapshot added with ID: " + event.getId());
                    Toast.makeText(CreateEventActivity.this,
                            R.string.create_event_complete_message, Toast.LENGTH_SHORT).show();
                    eventName.setEnabled(true);
                    eventName.getText().clear();
                    eventDescription.setEnabled(true);
                    eventDescription.getText().clear();
                    eventDateTime.setEnabled(true);
                    eventDateTime.getText().clear();
                    eventLocation.setEnabled(true);
                    eventLocation.getText().clear();
                    eventInterests.setEnabled(true);
                    eventInterests.getText().clear();
                    eventItems = new ArrayList<>();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error adding document", e);
                }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
