package com.example.heychefv7;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<Post> postList;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Set Food Name & Description
        holder.txtFoodName.setText(post.getFoodName());
        holder.txtDescription.setText(post.getFoodDesc());

        // Load Image using Glide
        Glide.with(context)
                .load(post.getImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.imgPost);

        // Fetch Publisher Name
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(post.getPublisher());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String username = snapshot.child("name").getValue(String.class);
                    holder.txtPublisher.setText(username != null ? username : "Unknown");
                } else {
                    holder.txtPublisher.setText("Unknown");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // Handle click to open Recipe Page
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeActivity.class);
            intent.putExtra("postId", post.getPostId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void updateList(List<Post> filteredList) {
        postList = filteredList;
        notifyDataSetChanged();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPost;
        TextView txtFoodName, txtDescription, txtPublisher;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPost = itemView.findViewById(R.id.imgPost);
            txtFoodName = itemView.findViewById(R.id.txtFoodName);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtPublisher = itemView.findViewById(R.id.txtPublisher);
        }
    }
}
