package com.example.poptheballoon.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.view.View;

import com.example.poptheballoon.R;

public class SoundHelper {

    private MediaPlayer mMusicPlayer;
    private MediaPlayer mMusicPlayer1;
    private MediaPlayer mMusicPlayer2;
    private SoundPool mSoundPool;
    private int mSoundID;
    private boolean mLoaded;
    private float mVolume;

    public SoundHelper(Activity activity) {

        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        float actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolume = actVolume / maxVolume;

        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttrib = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSoundPool = new SoundPool.Builder().setAudioAttributes(audioAttrib).setMaxStreams(6).build();
        } else {
            //noinspection deprecation
            mSoundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        }

        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                mLoaded = true;
            }
        });
        mSoundID = mSoundPool.load(activity, R.raw.pop, 1);
    }

    public void playSound() {
        if (mLoaded) {
            mSoundPool.play(mSoundID, mVolume, mVolume, 1, 0, 1f);
        }
    }

    public void prepareMusicPlayer(Context context){
        mMusicPlayer = MediaPlayer.create(context.getApplicationContext(),
                R.raw.tune);
        mMusicPlayer.setVolume(3f,3f);
        mMusicPlayer.setLooping(true);
    }

    public void prepareMusicPlayer1(Context context){
        mMusicPlayer1 = MediaPlayer.create(context.getApplicationContext(),
                R.raw.gameover1);
        mMusicPlayer1.setVolume(10f,10f);
        //mMusicPlayer1.setLooping(true);
    }

    public void prepareMusicPlayer2(Context context){
        mMusicPlayer2 = MediaPlayer.create(context.getApplicationContext(),
                R.raw.start_music);
        mMusicPlayer2.setVolume(10f,10f);
        mMusicPlayer2.setLooping(true);
    }


    public void playMusic(){
        if(mMusicPlayer != null){
            mMusicPlayer.start();
        }
    }

    public void playMusic1(){
        if(mMusicPlayer1 != null){
            mMusicPlayer1.start();
        }
    }

    public void playMusic2(){
        if(mMusicPlayer2 != null){
            mMusicPlayer2.start();
        }
    }

    public void pauseMusic(){
        if(mMusicPlayer != null && mMusicPlayer.isPlaying()) {
            mMusicPlayer.pause();
        }
    }
    public void pauseMusic1(){
        if(mMusicPlayer1 != null && mMusicPlayer1.isPlaying()) {
            mMusicPlayer1.pause();
        }
    }
    public void pauseMusic2(){
        if(mMusicPlayer2 != null && mMusicPlayer2.isPlaying()) {
            mMusicPlayer2.pause();
        }
    }
}
