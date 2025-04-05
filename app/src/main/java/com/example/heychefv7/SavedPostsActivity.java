package com.example.heychefv7;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.heychefv7.PostAdapter;
import com.example.heychefv7.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SavedPostsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private List<String> savedPostIds;

    private FirebaseUser firebaseUser;

    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_posts);

        recyclerView = findViewById(R.id.recyclerSavedPosts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnBack = findViewById(R.id.btnBackSaved);
        btnBack.setOnClickListener(v -> onBackPressed());

        postList = new ArrayList<>();
        savedPostIds = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        recyclerView.setAdapter(postAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        getSavedPosts();
    }

    private void getSavedPosts() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users")
                .child(firebaseUser.getUid())
                .child("savedPosts");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                savedPostIds.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    savedPostIds.add(snap.getKey());
                }
                loadSavedPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SavedPostsActivity.this, "Failed to load saved posts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSavedPosts() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        postsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Post post = snap.getValue(Post.class);
                    if (post != null && savedPostIds.contains(post.getPostId())) {
                        postList.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SavedPostsActivity.this, "Error loading posts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
