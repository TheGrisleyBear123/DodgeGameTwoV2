package com.example.dodgegametwo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
public class Missile {
    float x, y;
    float speedY;
    Bitmap bitmap;

    public Missile(float x, float y, Bitmap bitmap, float speedY) {
        this.x = x;
        this.y = y;
        this.bitmap = bitmap;
        this.speedY = speedY;
    }


    public void update() {
        y -= speedY;

    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, x, y, null);
    }
}
