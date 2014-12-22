package com.mzule.audiodemo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener {
    private static final int PROGRESS_UPDATE_PERIOD = 100;
    private static final int MSG_UPDATE_PROGRESS = 1;

    @InjectView(R.id.progressBar) ProgressBar progressBar;
    @InjectView(R.id.timeView) TextView timeView;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
    private boolean isStopped;
    private int duration;
    private int currentPosition;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            updatePercent();
            if (!isStopped) {
                handler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, PROGRESS_UPDATE_PERIOD);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        try {
            mediaPlayer.setDataSource("http://huohua-static.qiniudn.com/apk_ccnl.mp3");
            mediaPlayer.prepare();
            mediaPlayer.start();
            duration = mediaPlayer.getDuration();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
        isStopped = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStopped = true;
    }

    private void updatePercent() {
        currentPosition = mediaPlayer.getCurrentPosition();
        progressBar.setProgress((int) (currentPosition * 100f / duration));
        timeView.setText(format(currentPosition) + "/" + format(duration));
    }

    private String format(int duration) {
        return dateFormat.format(duration);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        progressBar.setSecondaryProgress(percent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.start();
    }
}
