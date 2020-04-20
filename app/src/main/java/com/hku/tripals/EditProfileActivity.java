package com.hku.tripals;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hku.tripals.model.User;
import com.mukesh.countrypicker.CountryPicker;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener{

    private static final String TAG = "EditProfileActivity";

    private User mUser;
    private ImageView close;
    private TextView Save;
    private Button avatarButton;
    private EditText username, bio;
    private TextView gender, birthday, homeCountry, language;

    private ImageView avatar;
    private Uri avatarUri;
    private Bitmap avaterBitmap;
    private String avatarImageUrl = "";
    private ProgressBar progressBar;

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private CountryPicker countryPicker;

    private TextView interests;
    private String[] listItems; //all interest options
    private boolean[] checkedItems; //check boxes
    private List<String> selectedInterest;
    private ArrayList<Integer> mUserItems = new ArrayList<>(); //show checked items

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        //initialize(); //date
        //setListener(); //date

        close = findViewById(R.id.edit_profile_close);
        username = findViewById(R.id.edit_profile_username_editText);
        gender = findViewById(R.id.edit_profile_gender);
        birthday = findViewById(R.id.edit_profile_birthday);
        homeCountry = findViewById(R.id.edit_profile_homeCountry);
        language = findViewById(R.id.edit_profile_language);
        interests = findViewById(R.id.edit_profile_interest);
        bio = findViewById(R.id.edit_profile_bio_editText);
        avatar = (ImageView) findViewById(R.id.edit_profile_avatar);

        listItems = getResources().getStringArray(R.array.interest_options);
        checkedItems = new boolean[listItems.length];
        Save = findViewById(R.id.edit_profile_save);
        avatarButton = (Button) findViewById(R.id.edit_profile_avatar_button);
        progressBar = (ProgressBar) findViewById(R.id.create_porfile_progressBar);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        retrieveUser();

        reference = FirebaseDatabase
                .getInstance()
                .getReference("user-profile")
                .child(currentUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User Auser = dataSnapshot.getValue(User.class);
                Log.d(TAG, "DataSnapshot user: " + Auser);

                //username.setText(user.getDisplayName());
                //bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if(currentUser.getPhotoUrl() != null){
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .circleCrop()
                    .into(avatar);
            avatarButton.setAlpha(0f);
        }

        avatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: select avatar clicked");
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 0);
            }
        });

        birthday.setOnClickListener(new View.OnClickListener() { //***Explanation Remarks***
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance(); //***Explanation Remarks***
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        EditProfileActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(avatarUri != null) {
                    uploadImageToFirebase();
                }else{
                    SaveButton();
                }
            }
        });

        /*mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String date = month + "/" + dayOfMonth + "/" + year;
                birthday.setText(date);
            }
        };*/

        interests.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog.Builder Interest_Builder = new AlertDialog.Builder(EditProfileActivity.this);
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
                        selectedInterest = new ArrayList<>();
                        for (int i=0; i<mUserItems.size(); i++){
                            item = item + listItems[mUserItems.get(i)];
                            selectedInterest.add(listItems[mUserItems.get(i)]);
                            if(i != mUserItems.size() -1) {
                                item = item + ", ";
                            }
                        }
                        interests.setText(item);
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
                            interests.setText("");
                        }
                    }
                });

                AlertDialog interestDialog = Interest_Builder.create();
                interestDialog.show();
            }
        });
    }

    private void retrieveUser(){

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("user-profile").document(currentUser.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                mUser= documentSnapshot.toObject(User.class);
                Log.d(TAG, "user.getDisplayName(): " + mUser.getDisplayName());
                Log.d(TAG, "user.getBio(): " + mUser.getBio());
                Log.d(TAG, "user.getGender(): " + mUser.getGender());
                Log.d(TAG, "user.getInterests(): " + mUser.getInterests().toString());
                Log.d(TAG, "user.getHomeCountry(): " + mUser.getHomeCountry());
                Log.d(TAG, "user.getLanguage(): " + mUser.getLanguage());

                username.setText(mUser.getDisplayName());
                gender.setText(mUser.getGender());
                birthday.setText(mUser.getBirthday());
                homeCountry.setText(mUser.getHomeCountry());
                language.setText(mUser.getLanguage());
                bio.setText(mUser.getBio());

                String interestList = mUser.getInterests().toString();
                String interestString = interestList.substring(1, interestList.length() - 1);
                interests.setText(interestString);
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
                gender.setText(male);
                return true;
            case R.id.Female:
                String female = "Female";
                Toast.makeText(this, "Female clicked", Toast.LENGTH_SHORT).show();
                gender.setText(female);
                return true;
            case R.id.Other:
                String other = "Other";
                Toast.makeText(this, "Other clicked", Toast.LENGTH_SHORT).show();
                gender.setText(other);
                return true;
            case R.id.English:
                String english = "English";
                Toast.makeText(this, "English clicked", Toast.LENGTH_SHORT).show();
                language.setText(english);
                return true;
            case R.id.Cantonese:
                String cantonese = "Cantonese";
                Toast.makeText(this, "Cantonese clicked", Toast.LENGTH_SHORT).show();
                language.setText(cantonese);
                return true;
            case R.id.Mandarin:
                String mandarin = "Mandarin";
                Toast.makeText(this, "Cantonese clicked", Toast.LENGTH_SHORT).show();
                language.setText(mandarin);
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

    /*public void initialize() { //***Explanation Remarks***
        homeCountry = findViewById(R.id.edit_profile_homeCountry);
        countryPicker =
                new CountryPicker.Builder().with(this)
                        .listener(this)
                        .build();
    }*/

    /*private void setListener() {
        homeCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countryPicker.showDialog(getSupportFragmentManager());
            }
        });
    }*/

    /*@Override
    public void onSelectCountry(Country country) { //***Explanation Remarks***
        String selectedHomeCountry = country.getName();
        homeCountry.setText(selectedHomeCountry);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d(TAG, "Photo selected");
            avatarUri = data.getData();
            try {
                avaterBitmap = decodeUri(this, avatarUri, 80);
            } catch (IOException e) {
                e.printStackTrace();
            }
            avatar.setImageBitmap(avaterBitmap);
            avatarButton.setAlpha(0f);
        }
    }

    private void uploadImageToFirebase(){
        if(avatarUri == null)
            return;
        String filename = currentUser.getUid();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        avaterBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final StorageReference ref = FirebaseStorage.getInstance().getReference("/avatar-images/"+filename);
        ref.putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "avatar photo uploaded");
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        avatarImageUrl = uri.toString();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(Uri.parse(avatarImageUrl))
                                .build();
                        currentUser.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            SaveButton();
                                            Log.d(TAG, "User profile updated.");
                                        }
                                    }
                                });
                        Log.d(TAG, "avatar url: "+avatarImageUrl);
                    }
                });
            }
        });
    }

    public boolean checkemptystring() {
        if (TextUtils.isEmpty(username.getText().toString())){
            return false;
        } else if (TextUtils.isEmpty(gender.getText().toString())){
            return false;
        } else if (TextUtils.isEmpty(birthday.getText().toString())){
            return false;
        } else if (TextUtils.isEmpty(homeCountry.getText().toString())){
            return false;
        } else if (TextUtils.isEmpty(language.getText().toString())){
            return false;
        } else if (TextUtils.isEmpty(bio.getText().toString())){
            return false;
        } else if (TextUtils.isEmpty(interests.getText().toString())){
            return false;
        } else if (avatar == null) {
            return false;
        } else {
            return true;
        }
    }

    public void SaveButton() {
        if (checkemptystring()) {
            updateProfile();
            Toast.makeText(this, "SavedProfileToFirebase", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
        }
    }

    //Save button (need amendment)
    public void updateProfile() {
        //progressBar.setVisibility(View.VISIBLE);
        // Do something in response to button click
        String uid = mAuth.getCurrentUser().getUid();
        Toast.makeText(this, "Saved profile", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Save button clicked.");
        Log.d(TAG, "uid: " + uid);

        //DocumentReference documentReference = fstore.collection("users").document(userID);
        User user = new User();
        user.setUid(uid);
        user.setDisplayName(mAuth.getCurrentUser().getDisplayName());

        if(avatarImageUrl.matches("")){
            user.setAvatarImageUrl(mAuth.getCurrentUser().getPhotoUrl().toString());
        }else{
            user.setAvatarImageUrl(avatarImageUrl);
        }
        user.setDisplayName(username.getText().toString());
        user.setGender(gender.getText().toString());
        user.setBirthday(birthday.getText().toString());
        user.setHomeCountry(homeCountry.getText().toString());
        user.setLanguage(language.getText().toString());
        user.setBio(bio.getText().toString());
        if ( selectedInterest != null) {
            user.setInterests(selectedInterest);

        } else {
            user.setInterests(mUser.getInterests());
        }

        Log.d(TAG, user.toString());

        //DatabaseReference refence = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUID());
        DocumentReference docRef = db.collection("user-profile").document(currentUser.getUid());

        db.collection("user-profile").document(uid)
                .set(user.toMap())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
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

}
