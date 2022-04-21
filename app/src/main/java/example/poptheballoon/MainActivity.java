package com.example.poptheballoon;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.poptheballoon.utils.HighScoreHelper;
import com.example.poptheballoon.utils.SoundHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity
    implements Balloon.BalloonListener {

    private static final int MIN_ANIMATION_DELAY = 500;
    private static final int MAX_ANIMATION_DELAY = 1500;
    private static final int MIN_ANIMATION_DURATION = 1000;
    private static final int MAX_ANIMATION_DURATION = 8000;
    private static final int NUMBER_OF_PINS = 5;
    private static final int BALLOONS_PER_LEVEL = 10;

    private ViewGroup mContentView;

    private int[] mBalloonColors = new int[5];
    private int mNextColor ,mScreenWidth ,mScreenHeight;

    TextView mScoreDisplay, mLevelDisplay;

    private int mLevel,mScore,mPinsUsed;
    private List<ImageView> mPinImages = new ArrayList<>();
    private List<Balloon> mBalloons = new ArrayList<>();
    private Button mGoButton;
    private boolean mPlaying;
    private boolean mGameStopped = true;
    private int mBalloonsPopped;
    private SoundHelper mSoundHelper;
    private SoundHelper mSoundHelper1;
    private SoundHelper mSoundHelper2;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBalloonColors[0] = Color.argb(255,0,0,120);
        mBalloonColors[1] = Color.argb(250,255,250,0);
        mBalloonColors[2] = Color.argb(255,180,21,66);
        mBalloonColors[3] = Color.argb(200,0,250,0);
        mBalloonColors[4] = Color.argb(100,250,10,250);

        getWindow().setBackgroundDrawableResource(R.drawable.modern_background);

        mContentView = (ViewGroup) findViewById(R.id.activity_main);
        setToFullScreen();

        //Getting width and height after going fullscreen
        ViewTreeObserver viewTreeObserver = mContentView.getViewTreeObserver();
        if(viewTreeObserver.isAlive()){
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onGlobalLayout() {
                    mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mScreenHeight = mContentView.getHeight();
                    mScreenWidth = mContentView.getWidth();
                }
            });
        }

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToFullScreen();
            }
        });

        fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        mScoreDisplay = (TextView) findViewById(R.id.score_display);
        mLevelDisplay = (TextView) findViewById(R.id.level_display);
        mPinImages.add((ImageView) findViewById(R.id.pushpin1));
        mPinImages.add((ImageView) findViewById(R.id.pushpin2));
        mPinImages.add((ImageView) findViewById(R.id.pushpin3));
        mPinImages.add((ImageView) findViewById(R.id.pushpin4));
        mPinImages.add((ImageView) findViewById(R.id.pushpin5));
        mGoButton =(Button) findViewById(R.id.go_button);

        int score = HighScoreHelper.getTopScore(this);
        TextView t = findViewById(R.id.textView);
        t.setText(String.format("NEW HIGHSCORE : %d",score));

        updateDisplay();

        mSoundHelper = new SoundHelper(this);
        mSoundHelper.prepareMusicPlayer(this);
        mSoundHelper1 = new SoundHelper(this);
        mSoundHelper1.prepareMusicPlayer1(this);
        mSoundHelper2 = new SoundHelper(this);
        mSoundHelper2.prepareMusicPlayer2(this);
        mSoundHelper2.playMusic2();
    }

    private void setToFullScreen(){
        ViewGroup rootLayout = (ViewGroup) findViewById(R.id.activity_main);
        rootLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setToFullScreen();
    }

    private void startGame(){
        setToFullScreen();
        mScore = 0;
        mLevel = 0;
        mPinsUsed = 0;
        for(ImageView pin : mPinImages){
            pin.setImageResource(R.drawable.pin);
        }
        mGameStopped = false;
        startLevel();
        //mSoundHelper.pauseMusic();
        //mSoundHelper.playMusic1();
        mSoundHelper2.pauseMusic2();
        mSoundHelper1.pauseMusic1();
        mSoundHelper.playMusic();
    }

    private void startLevel(){
        mLevel++;
        updateDisplay();
        BalloonLauncher launcher = new BalloonLauncher();
        launcher.execute(mLevel);
        mPlaying = true;
        mBalloonsPopped = 0;
        mGoButton.setText("STOP GAME");
    }

    private void finishLevel(){
        DefaultMessage1();
        mPlaying = false;
        mGoButton.setText(String.format("START LEVEL %d",mLevel+1));
    }

    public void DefaultMessage1()
    {
        Context context = getApplicationContext();
        // Create layout inflator object to inflate toast.xml file
        LayoutInflater inflater = getLayoutInflater();

        // Call toast.xml file for toast layout
        View toast1 = inflater.inflate(R.layout.toast1, null);

        Toast toast = new Toast(context);

        // Set layout to toast
        toast.setView(toast1);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
                0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }


    public void goButtonClickHandler(View view) {
        if(mPlaying){
            //user is playing the game and wants to stop
            gameOver(false);
        }else if(mGameStopped){
            //false if in-between levels or playing a level and true if not playing at all
            startGame();
        }else{
            //in-between levels
            startLevel();
        }
    }

    @Override
    public void popBalloon(Balloon balloon, boolean userTouch) {

        mBalloonsPopped++;
        mSoundHelper.playSound();

        mContentView.removeView(balloon);
        mBalloons.remove(balloon);
        if(userTouch){
            mScore++;
        }else{
            mPinsUsed++;
            if(mPinsUsed <= mPinImages.size()){
                mPinImages.get(mPinsUsed - 1).setImageResource(R.drawable.pin_off);
            }
            if(mPinsUsed == NUMBER_OF_PINS){

                gameOver(true);
                return;
            }
        }
        updateDisplay();
        
        if(mBalloonsPopped == BALLOONS_PER_LEVEL){
            finishLevel();
        }
    }

    private void gameOver(boolean allPinsUsed) {
        DefaultMessage();
        mSoundHelper.pauseMusic();
        mSoundHelper1.playMusic1();

        for(Balloon balloon: mBalloons){
            mContentView.removeView(balloon);
            balloon.setPopped(true);
        }
        mBalloons.clear();
        mPlaying = false;
        mGameStopped = true;
        mGoButton.setText("RESTART GAME");

        if(allPinsUsed){
            if(HighScoreHelper.isTopScore(this,mScore)){
                //User gets new High Score
                HighScoreHelper.setTopScore(this,mScore);
                TextView textView = (TextView) findViewById(R.id.textView);
                textView.setText(String.format("NEW HIGH SCORE: %d",mScore));
                HighScoreMessage();
            }
        }
    }

    public void DefaultMessage()
    {
        Context context = getApplicationContext();
        // Create layout inflator object to inflate toast.xml file
        LayoutInflater inflater = getLayoutInflater();

        // Call toast.xml file for toast layout
        View toast1 = inflater.inflate(R.layout.toast, null);

        Toast toast = new Toast(context);

        // Set layout to toast
        toast.setView(toast1);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
                0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    public void HighScoreMessage()
    {
        Context context = getApplicationContext();
        // Create layout inflator object to inflate toast.xml file
        LayoutInflater inflater = getLayoutInflater();

        // Call toast.xml file for toast layout
        View toast1 = inflater.inflate(R.layout.toast3, null);

        Toast toast = new Toast(context);

        // Set layout to toast
        toast.setView(toast1);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
                0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

    private void updateDisplay() {
        mScoreDisplay.setText(String.valueOf(mScore));
        mLevelDisplay.setText(String.valueOf(mLevel));
    }

    //pasted code
    private class BalloonLauncher extends AsyncTask<Integer, Integer, Void> {

        @Override
        protected Void doInBackground(Integer... params) {

            if (params.length != 1) {
                throw new AssertionError(
                        "Expected 1 param for current level");
            }

            int level = params[0];
            int maxDelay = Math.max(MIN_ANIMATION_DELAY,
                    (MAX_ANIMATION_DELAY - ((level - 1) * 500)));
            int minDelay = maxDelay / 2;

            int balloonsLaunched = 0;
            while (mPlaying && balloonsLaunched < BALLOONS_PER_LEVEL) {

//              Get a random horizontal position for the next balloon
                Random random = new Random(new Date().getTime());
                int xPosition = random.nextInt(mScreenWidth - 200);
                publishProgress(xPosition);
                balloonsLaunched++;

//              Wait a random number of milliseconds before looping
                int delay = random.nextInt(minDelay) + minDelay;
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int xPosition = values[0];
            launchBalloon(xPosition);
        }

    }

    private void launchBalloon(int x) {

        Balloon balloon = new Balloon(this, mBalloonColors[mNextColor], 150);
        mBalloons.add(balloon);

        if (mNextColor + 1 == mBalloonColors.length) {
            mNextColor = 0;
        } else {
            mNextColor++;
        }

//      Set balloon vertical position and dimensions, add to container
        balloon.setX(x);
        balloon.setY(mScreenHeight + balloon.getHeight());
        mContentView.addView(balloon);

//      Let 'er fly
        int duration = Math.max(MIN_ANIMATION_DURATION, MAX_ANIMATION_DURATION - (mLevel * 1000));
        balloon.releaseBalloon(mScreenHeight, duration);

    }

    private void showDialog(){
        InstructionsDialog dialog = new InstructionsDialog();
        dialog.show(getSupportFragmentManager(),"INSTRUCTION_DIALOG");
    }
}
