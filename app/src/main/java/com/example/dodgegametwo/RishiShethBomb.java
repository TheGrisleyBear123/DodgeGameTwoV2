package com.example.dodgegametwo;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class Bomb {
    float x, y;
    Bitmap bitmap;
    Bitmap hitBitmap; // Bitmap to display when hit
    boolean isHit = false;
    long hitTime;
    static final long HIT_DURATION = 2000; // 2 seconds hit image display

    public Bomb(float x, float y, Bitmap bitmap) {
        this.x = x;
        this.y = y;
        this.bitmap = bitmap;

    }

    public void update() {
        // Logic to move the bomb
        y += 5; // Example of moving bomb down

        if (isHit && System.currentTimeMillis() - hitTime > HIT_DURATION) {
            isHit = false;
        }
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y, null);
    }

    public void hit() {
        isHit = true;
        hitTime = System.currentTimeMillis();
    }
}
