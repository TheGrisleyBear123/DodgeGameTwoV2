package com.example.dodgegametwo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.security.Key;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    //Code from this program has been used from Beginning Android Games
    //Review SurfaceView, Canvas, continue

    View layout;
    private SoundPool soundPool;
    private int explosionSoundId;
    GameSurface gameSurface;
    public boolean isGameSpeed = false;
    public float GameSpeed = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        layout =findViewById(R.id.id_layout);
        setContentView(gameSurface);
    }


    @Override
    protected void onPause() {
        super.onPause();
        gameSurface.pause();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameSurface.resume();
    }


    //----------------------------GameSurface Below This Line--------------------------//
    public class GameSurface extends SurfaceView implements Runnable, SensorEventListener {

        private long lastFireTime = 0;
        private static final long FIRE_COOLDOWN = 500;
        GameObject player;
        GameObject enemy;
        Boolean spawnEnemies;
        View layout;
        Canvas canvas;
        boolean isExploded = false;
        int explosionTimer; // Counts down to remove the explosion
        static final int EXPLOSION_LIFETIME = 30;
        private Bitmap explosionBitmap;
        float GameSpeed;
        private List<Missile> missiles = new ArrayList<>();
        private Bitmap missileBitmap;
        private MediaPlayer backroundMusicPlayer;
        private static final long BOMB_SPAWN_INTERVAL = 5000;
        private long lastSpawnTime;
        private List<Bomb> bombs = new ArrayList<>();
        int timerEnemy;
        Thread gameThread;
        SurfaceHolder holder;
        Bitmap bar;
        Bitmap bombBitmap;
        volatile boolean running = false;
        int barX = 0;
        SensorManager sensorManager;
        Sensor accelerometerSensor;
        Paint paintProperty;
        int screenWidth;
        int screenHeight;
        int randomX;
        private int score = 0;
        private Paint scorePaint;





        public GameSurface(MainActivity context) {
            super(context);

            player = new GameObject(BitmapFactory.decodeResource(getResources(), R.drawable.bar));
            enemy = new GameObject(BitmapFactory.decodeResource(getResources(), R.drawable.bombimage));

            spawnEnemies = true;
            scorePaint = new Paint();
            scorePaint.setColor(Color.WHITE); // Set the text color to white
            scorePaint.setTextSize(60); // Set the text size
            scorePaint.setTextAlign(Paint.Align.LEFT); // Align text to the left

            layout = findViewById(R.id.id_layout);
            explosionBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.finalexplos);
            GameSpeed = 1.0f;
            missileBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.missile);
            soundPool = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .build();
            explosionSoundId = soundPool.load(context, R.raw.oof, 1);

            lastSpawnTime = System.currentTimeMillis();
            setFocusable(true);
            holder = getHolder();
             long lastSpawnTime = System.currentTimeMillis();

             //music
             backroundMusicPlayer = MediaPlayer.create(this.getContext(), R.raw.stardust);
             backroundMusicPlayer.setLooping(true);
             backroundMusicPlayer.start();

            timerEnemy = 10;

            bar = BitmapFactory.decodeResource(getResources(), R.drawable.bar);
            bombBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bombimage);

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth = sizeOfScreen.x;
            screenHeight = sizeOfScreen.y;

            paintProperty = new Paint();
            paintProperty.setTextSize(100);


        }


        @Override
        public void run() {
            while (running) {


                if (!holder.getSurface().isValid())
                    continue;
                updateGame();

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSpawnTime >= BOMB_SPAWN_INTERVAL) {
                    spawnEnemy();
                    lastSpawnTime = currentTime;
                }
                Canvas canvas = holder.lockCanvas();
                canvas.drawRGB(255, 0, 0);
                updateBombs();
                drawBombs(canvas);
                updateAndDrawMissiles(canvas);

                drawScore(canvas);
                if(score == 10) {
                    spawnEnemies = false;
                }


                canvas.drawBitmap(bar, barX, screenHeight - 300, null);


                holder.unlockCanvasAndPost(canvas);
            }
        }




    public void resume() {
        running = true;
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
        gameThread = new Thread(this);
        gameThread.start();
        if(backroundMusicPlayer != null && !backroundMusicPlayer.isPlaying()) {
            backroundMusicPlayer.start();
        }
    }






    public void pause() {
        running = false;
        while (true) {
            try {
                gameThread.join();
                sensorManager.unregisterListener(this);
                break;
            } catch (InterruptedException e) {

            }
        }
        if(backroundMusicPlayer != null && backroundMusicPlayer.isPlaying()) {
            backroundMusicPlayer.pause();
        }
    }

        public boolean missileHitsBomb(Missile missile, Bomb bomb) {
            // Create rectangles from missile and bomb bitmap dimensions and positions
            Rect missileRect = new Rect((int) missile.x, (int) missile.y,
                    (int) missile.x + missile.bitmap.getWidth(),
                    (int) missile.y + missile.bitmap.getHeight());
            Rect bombRect = new Rect((int) bomb.x, (int) bomb.y,
                    (int) bomb.x + bomb.bitmap.getWidth(),
                    (int) bomb.y + bomb.bitmap.getHeight());

            // Check if rectangles overlap
            return missileRect.intersect(bombRect);
        }




        public void fireMissile() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFireTime >= FIRE_COOLDOWN) {
                float missileX = barX + bar.getWidth() / 2 - missileBitmap.getWidth() / 2;
                float missileY = screenHeight - bar.getHeight() - missileBitmap.getHeight(); // Adjust as necessary
                float missileSpeedY = 15; // Speed of the missile

                Missile missile = new Missile(missileX, missileY, missileBitmap, missileSpeedY);
                missiles.add(missile);
                lastFireTime = currentTime; // Update the last fire time
            }
        }

        public void updateAndDrawMissiles(Canvas canvas) {
            Iterator<Missile> missileIterator = missiles.iterator();
            while (missileIterator.hasNext()) {
                Missile missile = missileIterator.next();
                missile.update();
                missile.draw(canvas);

                Iterator<Bomb> bombIterator = bombs.iterator();
                while (bombIterator.hasNext()) {
                    Bomb bomb = bombIterator.next();
                    if (missileHitsBomb(missile, bomb)) {
                        // Collision detected, increase score
                        score++;
                        bombIterator.remove(); // Remove the bomb after hit
                        missileIterator.remove(); // Remove the missile after hit
                        break; // Exit the loop after handling collision to avoid ConcurrentModificationException
                    }
                }

                // Optional: Remove missiles that go off the screen
                if (missile.y + missile.bitmap.getHeight() < 0) {
                    missileIterator.remove();
                }
            }
        }




        public void spawnEnemy(){
            if(spawnEnemies == true) {
                randomX = (int) (Math.random() * bar.getWidth() * 2 + 1);
                bombs.add(new Bomb(randomX, 0, bombBitmap));
            }
    }

        private void updateBombs() {
            for (Bomb bomb : bombs) {
                bomb.update();
            }
        }

        private void drawBombs(Canvas canvas) {
            for (Bomb bomb : bombs) {
                bomb.draw(canvas);
            }
        }

        private void drawScore(Canvas canvas){
            canvas.drawText(String.valueOf(score), 20, 60, scorePaint); // Draw the score at the top left
        }

        public boolean bombHitsBar(Bomb bomb, Bitmap bar, int barX, int barY) {
            return bomb.x < barX + bar.getWidth() &&
                    bomb.x + bomb.bitmap.getWidth() > barX &&
                    bomb.y < barY + bar.getHeight() &&
                    bomb.y + bomb.bitmap.getHeight() > barY;
        }
        public void updateGame() {
            int barY = screenHeight - bar.getHeight();
            Iterator<Bomb> iterator = bombs.iterator();

            while (iterator.hasNext()) {
                Bomb bomb = iterator.next();
                bomb.update();

                if (bombHitsBar(bomb, bar, barX, barY)) {
                    soundPool.play(explosionSoundId, 1, 1, 1, 0, 1f);
                    score++;
                    bombBitmap = explosionBitmap;
                    Log.d("HH", String.valueOf(score));
                    iterator.remove();
                } 
            }
        }



        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) { // Only trigger on the initial touch down
                fireMissile();
                return true; // Indicate that the touch event has been handled
            }
            return super.onTouchEvent(event); // Handle other touch events if any
        }







        @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //tilt position
            float tilt = event.values[0];

            //tilt bar
            barX -= (int) (tilt * 5);

            //bounds
            if (barX < 0) {
                barX = 0;
            } else if (barX + bar.getWidth() > screenWidth) {
                barX = screenWidth - bar.getWidth();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    }//GameSurface
}//Activity