package com.example.dodgegametwo;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class GameObject {
    private Bitmap normalImage;
    private Bitmap hitImage;
    private Bitmap currentImage;
    private boolean isHit;
    private long hitTime;
    private static final long HIT_DURATION = 1000; // Duration in milliseconds

    public GameObject(Bitmap normalImage) {
        this.normalImage = normalImage;
        this.hitImage = hitImage;
        this.currentImage = normalImage; // Start with the normal image
        this.isHit = false;
    }

    // Method to be called when the object is hit
    public void onHit() {
        currentImage = hitImage; // Swap to the hit image
        isHit = true;
        hitTime = System.currentTimeMillis(); // Record the time of the hit
    }

    // Method to update the object's state
    public void update() {
        if (isHit && System.currentTimeMillis() - hitTime > HIT_DURATION) {
            // After the hit duration has passed
            currentImage = normalImage; // Revert to the normal image
            isHit = false;
            // Optionally, remove the object or move it off-screen
        }
        // Other update logic, like moving the object...
    }

    // Method to draw the object
    public void draw(Canvas canvas, float x, float y) {
        canvas.drawBitmap(currentImage, x, y, null);
    }
}
