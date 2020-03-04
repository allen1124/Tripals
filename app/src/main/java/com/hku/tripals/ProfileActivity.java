package com.hku.tripals;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;
import com.mukesh.countrypicker.OnCountryPickerListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, OnCountryPickerListener {

    //Class write there.

    private static final String TAG = "ProfileActivity";
    private TextView DisplayGender;
    private TextView DisplayBirthDate;
    private TextView DisplayLanguage;
    private TextView DisplayHomeCountry;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private CountryPicker countryPicker;
    private TextView DisplayBio;
    private Button CreateBtn;

    private TextView DisplayInterest;
    private String[] listItems; //all interest options
    private boolean[] checkedItems; //check boxes
    private ArrayList<Integer> mUserItems = new ArrayList<>(); //show checked items

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initialize();
        setListener();

        DisplayBirthDate = (TextView) findViewById(R.id.CreateP_SelectBirthDate);
        DisplayGender = (TextView) findViewById(R.id.CreateP_SelectGender);
        DisplayLanguage = (TextView) findViewById(R.id.CreateP_SelectLanguage);
        DisplayHomeCountry = (TextView) findViewById(R.id.CreateP_SelectHomeCountry);
        DisplayInterest = (TextView) findViewById(R.id.CreateP_SelectInterest);
        listItems = getResources().getStringArray(R.array.interest_options);
        checkedItems = new boolean[listItems.length];
        DisplayBio = (TextView) findViewById(R.id.CreateP_InputBio);
        CreateBtn = (Button) findViewById(R.id.CreateProfileButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();




        DisplayBirthDate.setOnClickListener(new View.OnClickListener() { //***Explanation Remarks***
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance(); //***Explanation Remarks***
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        ProfileActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = month + "/" + dayOfMonth + "/" + year;
                DisplayBirthDate.setText(date);
            }
        };

        DisplayInterest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog.Builder Interest_Builder = new AlertDialog.Builder(ProfileActivity.this);
                Interest_Builder.setTitle("Select Interest");
                Interest_Builder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                        if (isChecked) {
                            if (!mUserItems.contains(position)) { //***Explanation Remarks***
                                mUserItems.add(position);
                            }
                        } else {
                            for (int i = 0; i < mUserItems.size(); i++)
                                if (mUserItems.get(i) == position) {
                                    mUserItems.remove(i);
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
                        for (int i=0; i<mUserItems.size(); i++){
                            item = item + listItems[mUserItems.get(i)];
                            if(i != mUserItems.size() -1) {
                                item = item + ", ";
                            }
                        }
                        DisplayInterest.setText(item);
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
                            mUserItems.clear();
                            DisplayInterest.setText("");
                        }
                    }
                });

                AlertDialog interestDialog = Interest_Builder.create();
                interestDialog.show();
            }
        });
    }

    //Gender Selection and Popup Menu

    public void selectGender(View v) {
        PopupMenu genderpopup = new PopupMenu(this, v);
        genderpopup.setOnMenuItemClickListener(this);
        genderpopup.inflate(R.menu.gender_menu);
        genderpopup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Male:
                String male = "Male";
                Toast.makeText(this, "Male clicked", Toast.LENGTH_SHORT).show();
                DisplayGender.setText(male);
                return true;
            case R.id.Female:
                String female = "Female";
                Toast.makeText(this, "Female clicked", Toast.LENGTH_SHORT).show();
                DisplayGender.setText(female);
                return true;
            case R.id.Other:
                String other = "Other";
                Toast.makeText(this, "Other clicked", Toast.LENGTH_SHORT).show();
                DisplayGender.setText(other);
                return true;
            case R.id.English:
                String english = "English";
                Toast.makeText(this, "English clicked", Toast.LENGTH_SHORT).show();
                DisplayLanguage.setText(english);
                return true;
            default:
                return false;
        }
    }

    //Language Selection and Popup Menu

    public void selectLanguage(View v) {
        PopupMenu languagepopup = new PopupMenu(this, v);
        languagepopup.setOnMenuItemClickListener(this);
        languagepopup.inflate(R.menu.language_menu);
        languagepopup.show();
    }

    //Country Selection and Popup Menu

    private void initialize() { //***Explanation Remarks***
        DisplayHomeCountry = findViewById(R.id.CreateP_SelectHomeCountry);
        countryPicker =
                new CountryPicker.Builder().with(this)
                        .listener(this)
                        .build();
    }

    private void setListener() {
        DisplayHomeCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countryPicker.showDialog(getSupportFragmentManager());
            }
        });
    }

    @Override
    public void onSelectCountry(Country country) { //***Explanation Remarks***
        String selectedHomeCountry = country.getName();
        DisplayHomeCountry.setText(selectedHomeCountry);
    }

    //Create button

    public void createProfile(View view) {
        // Do something in response to button click
        String uid = mAuth.getCurrentUser().getUid();
        Toast.makeText(this, "Created profile", Toast.LENGTH_SHORT).show();
        Log.d("CREATEPROFILE", "Create button clicked.");
        Log.d("uid", "uid: "+uid);
        //DocumentReference documentReference = fstore.collection("users").document(userID);

        Map<String, Object> user = new HashMap<>();
        user.put("gender", DisplayGender.getText().toString());
        user.put("birthday", DisplayBirthDate.getText().toString());
        user.put("homeCountry", DisplayHomeCountry.getText().toString());
        user.put("language", DisplayLanguage.getText().toString());
        user.put("bio", DisplayBio.getText().toString());

        Map<String, Object> interestData = new HashMap<>();
        interestData.put("interestString", DisplayInterest.getText().toString());
        interestData.put("interestCheckedList", mUserItems);

        user.put("interest", interestData);

        Log.d(TAG, user.toString());

        db.collection("user-profile").document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

    }
}