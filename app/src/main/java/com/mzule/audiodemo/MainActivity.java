package com.mzule.audiodemo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnErrorListener, AudioService.OnAudioProgressUpdateListener {

    @InjectView(R.id.progressBar) ProgressBar progressBar;
    @InjectView(R.id.timeView) TextView timeView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
    private AudioService audioService;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioBinder audioBinder = (AudioService.AudioBinder) service;
            audioService = audioBinder.getService();
            audioService.setListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        Intent intent = new Intent(getApplicationContext(), AudioService.class);
        intent.setAction(AudioService.ACTION_PLAY);
        intent.putExtra("url", "http://huohua-static.qiniudn.com/apk_ccnl.mp3");
        bindService(intent, conn, 0);
        startService(intent);
        Log.d("startService", "startService");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (audioService != null) {
            audioService.setListener(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (audioService != null) {
            audioService.setListener(null);
        }
    }

    private String format(int duration) {
        return dateFormat.format(duration);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        progressBar.setSecondaryProgress(percent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getApplicationContext(), "error playing audio", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void update(int duration, int currentPosition) {
        progressBar.setProgress((int) (currentPosition * 100f / duration));
        timeView.setText(format(currentPosition) + "/" + format(duration));
    }
}
