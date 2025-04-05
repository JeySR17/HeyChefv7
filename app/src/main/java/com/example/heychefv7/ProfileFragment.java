package com.example.heychefv7;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private ImageView imgProfile;
    private TextView txtUsername, txtEmail;
    private Button btnEditProfile;
    private ImageButton btnLogout;
    private FloatingActionButton btnAddPost, btnSavedPosts;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        // Initialize Views
        imgProfile = view.findViewById(R.id.imgProfile);
        txtUsername = view.findViewById(R.id.txtUsername);
        txtEmail = view.findViewById(R.id.txtEmail);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnAddPost = view.findViewById(R.id.btnAddPost);
        btnSavedPosts = view.findViewById(R.id.btnSavedPosts);

        loadProfileDetails();

        // Logout Button
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        // Edit Profile Button
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ProfileActivity.class));
        });

        // Add Post Button
        btnAddPost.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        });

        // Saved Posts Button
        btnSavedPosts.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SavedPostsActivity.class));
        });

        return view;
    }

    private void loadProfileDetails() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String profileImage = snapshot.child("profilePic").getValue(String.class);

                    txtUsername.setText(username != null ? username : "No Username");
                    txtEmail.setText(email != null ? email : "No Email");

                    if (profileImage != null && !profileImage.isEmpty()) {
                        Glide.with(requireContext())
                                .load(profileImage)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .circleCrop()
                                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .into(imgProfile);
                    } else {
                        imgProfile.setImageResource(R.drawable.ic_profile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileDetails();
    }
}
