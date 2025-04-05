package com.example.heychefv7;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class AddPostActivity extends AppCompatActivity {

    private ImageView imgFood;
    private EditText edtFoodName, edtFoodDesc, editTextIngredients, editTextInstructions;
    private Button btnPost;
    private Uri imageUri;
    private FirebaseAuth mAuth;
    private DatabaseReference postsRef;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // hide default title
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);   // enable back arrow
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        storageRef = FirebaseStorage.getInstance().getReference("PostImages");

        // Initialize Views
        imgFood = findViewById(R.id.imgFood);
        edtFoodName = findViewById(R.id.edtFoodName);
        edtFoodDesc = findViewById(R.id.edtFoodDesc);
        editTextIngredients = findViewById(R.id.editTextIngredients);
        editTextInstructions = findViewById(R.id.editTextInstructions);
        btnPost = findViewById(R.id.btnPost);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting...");

        // Image Picker
        imgFood.setOnClickListener(v -> openGallery());

        // Post Button
        btnPost.setOnClickListener(v -> uploadPost());
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
            imgFood.setImageURI(imageUri);
        }
    }

    private void uploadPost() {
        String foodName = edtFoodName.getText().toString().trim();
        String foodDesc = edtFoodDesc.getText().toString().trim();
        String ingredientsRaw = editTextIngredients.getText().toString().trim();
        String instructionsRaw = editTextInstructions.getText().toString().trim();

        if (imageUri == null || foodName.isEmpty() || foodDesc.isEmpty()
                || ingredientsRaw.isEmpty() || instructionsRaw.isEmpty()) {
            Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        String postId = postsRef.push().getKey();
        StorageReference fileRef = storageRef.child(postId + ".jpg");

        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    savePostToDatabase(postId, foodName, foodDesc, downloadUrl, ingredientsRaw, instructionsRaw);
                })
        ).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(AddPostActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void savePostToDatabase(String postId, String foodName, String foodDesc,
                                    String imageUrl, String ingredients, String instructions) {

        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("postId", postId);
        postMap.put("foodName", foodName);
        postMap.put("foodDesc", foodDesc);
        postMap.put("imageUrl", imageUrl);
        postMap.put("publisher", mAuth.getCurrentUser().getUid());
        postMap.put("ingredients", ingredients);
        postMap.put("instructions", instructions);

        postsRef.child(postId).setValue(postMap).addOnCompleteListener(task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                Toast.makeText(AddPostActivity.this, "Post uploaded successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AddPostActivity.this, "Failed to upload post.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle back button in toolbar
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
