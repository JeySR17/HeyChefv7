package com.example.heychefv7;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.List;

public class RecipeActivity extends AppCompatActivity {

    private ImageView imgRecipe, btnSaveRecipe;
    private TextView txtRecipeName, txtDescription, txtChef, txtIngredients, txtInstructions;
    private String postId, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // Setup custom toolbar with back button
        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        // Bind UI
        imgRecipe = findViewById(R.id.imgRecipe);
        txtRecipeName = findViewById(R.id.txtRecipeName);
        txtDescription = findViewById(R.id.txtRecipeDescription);
        txtChef = findViewById(R.id.txtChef);
        txtIngredients = findViewById(R.id.txtIngredients);
        txtInstructions = findViewById(R.id.txtInstructions);
        btnSaveRecipe = findViewById(R.id.btnSaveRecipe);

        postId = getIntent().getStringExtra("postId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadRecipeDetails();
        checkIfSaved(postId);

        btnSaveRecipe.setOnClickListener(v -> toggleSaveUnsave(postId));
    }

    private void loadRecipeDetails() {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("Posts").child(postId);
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String foodName = snapshot.child("foodName").getValue(String.class);
                    String foodDesc = snapshot.child("foodDesc").getValue(String.class);
                    String imageUrl = snapshot.child("imageUrl").getValue(String.class);
                    String publisher = snapshot.child("publisher").getValue(String.class);
                    String ingredients = snapshot.child("ingredients").getValue(String.class);
                    String instructions = snapshot.child("instructions").getValue(String.class);

                    txtRecipeName.setText(foodName);
                    txtDescription.setText(foodDesc);
                    Glide.with(RecipeActivity.this).load(imageUrl).into(imgRecipe);

                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(publisher);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String chefName = snapshot.child("name").getValue(String.class);
                            txtChef.setText("Chef: " + (chefName != null ? chefName : "Unknown"));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });

                    //txtIngredients.setText(ingredients != null ? ingredients : "No ingredients listed.");
                    //txtInstructions.setText(instructions != null ? instructions : "No instructions listed.");

                    // Format and set ingredients
                    if (ingredients != null && !ingredients.isEmpty()) {
                        txtIngredients.setText("• " + ingredients.replace("\n", "\n• "));
                    } else {
                        txtIngredients.setText("No ingredients listed.");
                    }

                    // Format and set instructions
                    if (instructions != null && !instructions.isEmpty()) {
                        txtInstructions.setText("• " + instructions.replace("\n", "\n• "));
                    } else {
                        txtInstructions.setText("No instructions listed.");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void checkIfSaved(String postId) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(currentUserId)
                .child("savedPosts");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(postId)) {
                    btnSaveRecipe.setImageResource(R.drawable.ic_bookmark_filled);
                    btnSaveRecipe.setTag("saved");
                } else {
                    btnSaveRecipe.setImageResource(R.drawable.ic_bookmark_outline);
                    btnSaveRecipe.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void toggleSaveUnsave(String postId) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(currentUserId)
                .child("savedPosts");

        if ("save".equals(btnSaveRecipe.getTag())) {
            ref.child(postId).setValue(true);
        } else {
            ref.child(postId).removeValue();
        }
    }
}
