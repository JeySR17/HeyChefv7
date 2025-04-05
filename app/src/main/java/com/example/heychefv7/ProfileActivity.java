package com.example.heychefv7;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgProfilePic;
    private EditText edtName, edtEmail;
    private Button btnSave;
    private Uri imageUri;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private FirebaseUser currentUser;

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Toolbar Setup
        ImageView backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> onBackPressed());

        imgProfilePic = findViewById(R.id.imgProfilePic);
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        btnSave = findViewById(R.id.btnSave);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating Profile...");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
        storageRef = FirebaseStorage.getInstance().getReference("ProfilePictures");

        loadUserData();

        imgProfilePic.setOnClickListener(v -> openGallery());
        btnSave.setOnClickListener(v -> updateUserProfile());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String profilePic = snapshot.child("profilePic").getValue(String.class);

                    edtName.setText(name);
                    edtEmail.setText(email);

                    if (profilePic != null && !profilePic.equals("default")) {
                        Glide.with(ProfileActivity.this)
                                .load(profilePic)
                                .into(imgProfilePic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imgProfilePic.setImageURI(imageUri);
        }
    }

    private void updateUserProfile() {
        String name = edtName.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        if (imageUri != null) {
            StorageReference fileRef = storageRef.child(currentUser.getUid() + ".jpg");
            fileRef.putFile(imageUri).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        updateProfileInDatabase(name, uri.toString());
                    });
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            updateProfileInDatabase(name, null);
        }
    }

    private void updateProfileInDatabase(String name, String profilePicUrl) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        if (profilePicUrl != null) {
            map.put("profilePic", profilePicUrl);
        }

        userRef.updateChildren(map).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                finish(); // Go back to MainActivity
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
