package com.mzule.audiodemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;

/**
 * Created by Lennnna on 12/23/14.
 */
public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    private static final int PROGRESS_UPDATE_PERIOD = 100;
    private static final int MSG_UPDATE_PROGRESS = 1;

    private static final String TAG = AudioService.class.getSimpleName();
    public static final String ACTION_AUDIO_CONTROL = "com.mzule.audiodemo.AUDIO.CONTROL";
    public static final String ACTION_PLAY = "com.mzule.audiodemo.PLAY";
    public static final String ACTION_PAUSE = "com.mzule.audiodemo.PAUSE";
    public static final String ACTION_RESUME = "com.mzule.audiodemo.RESUME";
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

    private void showNotification(String action) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.item_notification);
        Intent intent = new Intent(getApplicationContext(), AudioService.class);
        if (ACTION_PAUSE.equals(action)) {
            intent.setAction(ACTION_RESUME);
            remoteViews.setImageViewResource(R.id.actionButton, R.drawable.player_icon_play);
        } else {
            intent.setAction(ACTION_PAUSE);
            remoteViews.setImageViewResource(R.id.actionButton, R.drawable.player_icon_pause);
        }
        intent.putExtra("url", "http://huohua-static.qiniudn.com/apk_ccnl.mp3");
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);

        Notification notification = new Notification.Builder(getApplicationContext()).setSmallIcon(R.drawable.ic_launcher).setOngoing(true).build();
        remoteViews.setTextViewText(R.id.textView, "you are best");
        remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);
        notification.contentView = remoteViews;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private void dismissNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        showNotification(intent.getAction());
        final String url = intent.getStringExtra("url");
        new Thread(new Runnable() {
            @Override public void run() {
                if (ACTION_PLAY.equals(intent.getAction())) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
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
                } else if (ACTION_PAUSE.equals(intent.getAction())) {
                    Log.d(TAG, "onStartCommand#run#action_pause");
                    mediaPlayer.pause();
                } else if (ACTION_RESUME.equals(intent.getAction())) {
                    Log.d(TAG, "onStartCommand#run#action_resume");
                    mediaPlayer.start();
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
        dismissNotification();
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
