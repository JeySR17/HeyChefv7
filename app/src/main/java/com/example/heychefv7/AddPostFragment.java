package com.example.heychefv7;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AddPostFragment extends Fragment {

    private Button btnAddPost;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);

        // Initialize the button
        btnAddPost = view.findViewById(R.id.btnAddPost);

        // Button to open AddPostActivity
        btnAddPost.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddPostActivity.class);
            startActivity(intent);
        });

        return view;
    }
}
