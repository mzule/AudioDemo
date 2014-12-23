package com.mzule.audiodemo;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Lennnna on 12/23/14.
 */
public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    private static final int PROGRESS_UPDATE_PERIOD = 100;
    private static final int MSG_UPDATE_PROGRESS = 1;

    private static final String TAG = AudioService.class.getSimpleName();
    public static final String ACTION_PLAY = "com.mzule.audiodemo.PLAY";
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private MediaPlayer.OnErrorListener onErrorListener;
    private MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener;

    private OnAudioProgressUpdateListener onAudioProgressUpdateListener;
    private int duration;
    private boolean isComplete;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (onAudioProgressUpdateListener != null) {
                Log.d(TAG, "onAudioProgressUpdateListener");
                onAudioProgressUpdateListener.update(duration, mediaPlayer.getCurrentPosition());
            }
            if (!isComplete) {
                handler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, PROGRESS_UPDATE_PERIOD);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return new AudioBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        final String url = intent.getStringExtra("url");
        mediaPlayer.stop();
        mediaPlayer.reset();
        new Thread(new Runnable() {
            @Override public void run() {
                if (ACTION_PLAY.equals(intent.getAction())) {
                    Log.d(TAG, "onStartCommand#run#action_play");
                    try {
                        mediaPlayer.setDataSource(url);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(AudioService.this);
                        mediaPlayer.setOnErrorListener(onErrorListener);
                        mediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
                        duration = mediaPlayer.getDuration();
                        handler.sendEmptyMessage(MSG_UPDATE_PROGRESS);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        isComplete = true;
        stopSelf();
        Log.d(TAG, "onCompletion");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener onErrorListener) {
        this.onErrorListener = onErrorListener;
    }

    public void setOnBufferingUpdateListener(MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener) {
        this.onBufferingUpdateListener = onBufferingUpdateListener;
    }

    public void setOnAudioProgressUpdateListener(OnAudioProgressUpdateListener onAudioProgressUpdateListener) {
        this.onAudioProgressUpdateListener = onAudioProgressUpdateListener;
    }

    public void setListener(Object listener) {
        setOnErrorListener((MediaPlayer.OnErrorListener) listener);
        setOnBufferingUpdateListener((MediaPlayer.OnBufferingUpdateListener) listener);
        setOnAudioProgressUpdateListener((OnAudioProgressUpdateListener) listener);
    }

    public class AudioBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    public interface OnAudioProgressUpdateListener {
        void update(int duration, int currentPosition);
    }
}
