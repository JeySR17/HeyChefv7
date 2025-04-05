package com.example.heychefv7;

import java.util.List;

public class Post {
    private String postId;
    private String imageUrl;
    private String foodDesc;
    private String foodName;
    private String publisher;
    private String ingredients;
    private String instructions;

    // Empty constructor required for Firebase
    public Post() {}

    public Post(String postId, String imageUrl, String foodDesc, String foodName, String publisher) {
        this.postId = postId;
        this.imageUrl = imageUrl;
        this.foodDesc = foodDesc;
        this.foodName = foodName;
        this.publisher = publisher;
        this.ingredients = ingredients;
        this.instructions = instructions;

    }

    // Getters and Setters
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getFoodDesc() { return foodDesc; }
    public void setFoodDesc(String foodDesc) { this.foodDesc = foodDesc; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getIngredients() { return ingredients; }

    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getInstructions() { return instructions; }

    public void setInstructions(String instructions) { this.instructions = instructions; }
}
